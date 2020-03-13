package simulation.culture.group;

import extra.ShnyPair;
import kotlin.Pair;
import shmp.random.RandomException;
import simulation.culture.Event;
import simulation.culture.aspect.*;
import simulation.culture.group.cultureaspect.*;
import simulation.culture.group.intergroup.Relation;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.group.request.TagRequest;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static shmp.random.RandomProbabilitiesKt.testProbability;
import static shmp.random.RandomCollectionsKt.*;
import static simulation.Controller.*;

/**
 * Takes responsibility of Group's cultural change.
 */
public class CulturalCenter {
    private AspectCenter aspectCenter;
    private CultureAspectCenter cultureAspectCenter;
    private List<Aspiration> aspirations = new ArrayList<>();
    private Group group;
    private List<Event> events = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();
    private GroupMemes memePool;

    private Meme currentMeme;

    Map<Group, Relation> relations = new HashMap<>();

    CulturalCenter(Group group, GroupMemes memePool, List<Aspect> aspects) {
        this.group = group;
        this.memePool = memePool;
        this.aspectCenter = new AspectCenter(group);
        this.cultureAspectCenter = new CultureAspectCenter(group, new HashSet<>());
        aspects.forEach(this::hardAspectAdd);
        aspectCenter.getAspectPool().getAspects().forEach(Aspect::swapDependencies);//TODO will it swap though?
    }

    List<Aspiration> getAspirations() {
        return aspirations;
    }

    public AspectCenter getAspectCenter() {
        return aspectCenter;
    }

    public CultureAspectCenter getCultureAspectCenter() {
        return cultureAspectCenter;
    }

    List<Request> getRequests() {
        return requests;
    }

    Set<Aspect> getChangedAspects() {
        return aspectCenter.getChangedAspects();
    }

    List<Event> getEvents() {
        return events;
    }

    public Set<CultureAspect> getCultureAspects() {
        return cultureAspectCenter.getCultureAspects();
    }

    List<Aspect> getNeighboursAspects() {
        List<Aspect> allExistingAspects = new ArrayList<>();
        for (Group neighbour : relations.keySet()) {
            allExistingAspects.addAll(neighbour.getAspects().stream()
                    .filter(aspect -> !(aspect instanceof ConverseWrapper)
                            || aspectCenter.getAspectPool().contains(((ConverseWrapper) aspect).aspect))
                    .collect(Collectors.toList()));
        }
        return allExistingAspects;
    }

    private List<CultureAspect> getNeighboursCultureAspects() {
        List<CultureAspect> allExistingAspects = new ArrayList<>();
        for (Group neighbour : relations.keySet()) {
            allExistingAspects.addAll(neighbour.getCulturalCenter().getCultureAspects());
        }
        return allExistingAspects;
    }

    public GroupMemes getMemePool() {
        return memePool;
    }

    public Meme getCurrentMeme() {
        return currentMeme;
    }

    void addMemeCombination(Meme meme) {
        memePool.addMemeCombination(meme);
    }

    void addAspiration(Aspiration aspiration) {
        if (aspirations.stream().noneMatch(aspir -> aspir.equals(aspiration))) {
            aspirations.add(aspiration);
        }
    }

    public boolean addAspect(Aspect aspect) {
        return aspectCenter.addAspect(aspect);
    }

    void hardAspectAdd(Aspect aspect) {
        aspectCenter.hardAspectAdd(aspect);
    }

    public void addResourceWant(Resource resource) {
        cultureAspectCenter.addCultureAspect(new AestheticallyPleasingObject(group, resource));
    }

    void updateRequests() {
        requests = new ArrayList<>();
        int foodFloor = group.population / group.getFertility() + 1;
        BiFunction<Pair<Group, ResourcePack>, Double, Void> foodPenalty = (pair, percent) -> {
            pair.getFirst().starve(percent);
            pair.getSecond().destroyAllResourcesWithTag(new ResourceTag("food"));
            return null;
        };
        BiFunction<Pair<Group, ResourcePack>, Double, Void> foodReward = (pair, percent) -> {
            pair.getFirst().population += ((int) (percent * pair.getFirst().population)) / 10 + 1;
            pair.getFirst().population = Math.min(pair.getFirst().population, group.getMaxPopulation());
            pair.getSecond().destroyAllResourcesWithTag(new ResourceTag("food"));
            return null;
        };
        requests.add(new TagRequest(group, new ResourceTag("food"), foodFloor,
                foodFloor + group.population / 100 + 1, foodPenalty, foodReward));

        if (group.getTerritory().getMinTemperature() < 0) {
            BiFunction<Pair<Group, ResourcePack>, Double, Void> warmthPenalty = (pair, percent) -> {
                pair.getFirst().freeze(percent);
                return null;
            };
            BiFunction<Pair<Group, ResourcePack>, Double, Void> warmthReward = (pair, percent) -> null;
            requests.add(new TagRequest(group, new ResourceTag("warmth"), group.population,
                    group.population, warmthPenalty, warmthReward));
        }

        cultureAspectCenter.getAspectRequests().forEach(this::addRequest);
    }

    void update() {
        tryToFulfillAspirations();
        aspectCenter.mutateAspects();
        createArtifact();
        cultureAspectCenter.useCultureAspects();
        cultureAspectCenter.addRandomCultureAspect();
        cultureAspectCenter.mutateCultureAspects();
    }

    void intergroupUpdate() {
        adoptAspects();
        adoptCultureAspects();
    }

    private void addRequest(Request request) {
        if (request != null) {
            requests.add(request);
        }
    }

    private void removeAspiration(Aspiration aspiration) {
        aspirations.remove(aspiration);
    }

    private void adoptAspects() {
        if (!session.isTime(session.groupTurnsBetweenAdopts)) {
            return;
        }
        List<Aspect> allExistingAspects = getNeighboursAspects().stream()
                .filter(aspect -> !getChangedAspects().contains(aspect))
                .collect(Collectors.toList());

        if (!allExistingAspects.isEmpty()) {
            try {
                Aspect aspect = randomElementWithProbability(
                        allExistingAspects,
                        a -> a.getUsefulness() * getNormalizedRelation(a.getGroup()),
                        session.random
                );
                if (addAspect(aspect)) {
                    group.addEvent(new Event(Event.Type.AspectGaining,
                            String.format("Group %s got aspect %s from group %s",
                                    group.name, aspect.getName(), aspect.getGroup().name),
                            "group", group));
                }
            } catch (Exception e) {
                if (e instanceof RandomException) {
                    int i = 0;//TODO
                }
            }
        }
    }

    private void adoptCultureAspects() {
        try {
            if (!session.isTime(session.groupTurnsBetweenAdopts)) {
                return;
            }
            List<CultureAspect> cultureAspects = getNeighboursCultureAspects().stream()
                    .filter(aspect -> !getCultureAspects().contains(aspect)).collect(Collectors.toList());
            if (!cultureAspects.isEmpty()) {
                CultureAspect aspect = randomElementWithProbability(
                        cultureAspects,
                        a -> getNormalizedRelation(a.getGroup()),
                        session.random
                );
                cultureAspectCenter.addCultureAspect(aspect);
            }
        } catch (NullPointerException e) {
            throw new RuntimeException();
        }
    }

    private double getNormalizedRelation(Group group) {
        return relations.containsKey(group) ? relations.get(group).getPositiveNormalized() : 2;
    }

    private void createArtifact() {
        if (testProbability(0.1, session.random)) {
            if (memePool.isEmpty()) {
                return;
            }
            List<ConverseWrapper> _l = new ArrayList<>(aspectCenter.getAspectPool().getMeaningAspects());
            if (_l.isEmpty()) {
                return;
            }
            ConverseWrapper _a = randomElement(_l, session.random);
            generateCurrentMeme();
            AspectResult result = _a.use(new AspectController(
                    1,
                    1,
                    new ResourceEvaluator(rp -> rp, ResourcePack::getAmount),
                    group,
                    true
            ));
            clearCurrentMeme();
            for (Resource resource : result.resources.resources) {
                if (resource.hasMeaning()) {
                    group.uniqueArtifacts.add(resource);
                } else {
                    group.resourcePack.add(resource);
                }
            }
        }
    }

    public void putCurrentMeme(String memeString) {
        currentMeme = memePool.getMeme(memeString);
    }

    public void generateCurrentMeme() {
        currentMeme = memePool.getValuableMeme();
    }

    public void clearCurrentMeme() {
        currentMeme = null;
    }

    public Meme getMeaning() {
        Meme meme = getCurrentMeme();
        return meme == null ? memePool.getValuableMeme() : meme;
    }

    private void tryToFulfillAspirations() {
        Optional<Aspiration> _o = getAspirations().stream().max((Comparator.comparingInt(o -> o.level)));
        if (_o.isPresent()) {
            Aspiration aspiration = _o.get();
            List<ShnyPair<Aspect, Group>> options = aspectCenter.findOptions(aspiration);
            if (options.isEmpty()) {
                return;
            }
            ShnyPair<Aspect, Group> pair = randomElement(options, session.random);
            addAspect(pair.first);
            removeAspiration(aspiration);
            if (pair.second == null) {
                group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                        " developed aspect " + pair.first.getName(), "group", this));
            } else {
                group.addEvent(
                        new Event(Event.Type.AspectGaining,
                                String.format(
                                        "Group %s took aspect %s from group %s",
                                        group.name,
                                        pair.first.getName(),
                                        pair.second.name
                                ),
                                "group",
                                this
                        ));
            }
        }
    }

    void finishUpdate() {
        aspectCenter.finishUpdate();
    }

    public void pushAspects() {
        aspectCenter.pushAspects();
    }

    public void initializeFromCenter(CulturalCenter culturalCenter) {//TODO da hell is this?
        culturalCenter.getAspectCenter().getAspectPool().getAll().forEach(this::addAspect);
    }
}
