package simulation.culture.group;

import kotlin.Pair;
import simulation.Event;
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

import static shmp.random.RandomProbabilitiesKt.testProbability;
import static shmp.random.RandomCollectionsKt.*;
import static simulation.Controller.*;

/**
 * Takes responsibility of Group's cultural change.
 */
public class CultureCenter {
    private AspectCenter aspectCenter;
    private CultureAspectCenter cultureAspectCenter;
    private List<Aspiration> aspirations = new ArrayList<>();
    private Group group;
    private List<Event> events = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();
    private GroupMemes memePool;

    private Meme currentMeme;

    Map<Group, Relation> relations = new HashMap<>();

    CultureCenter(Group group, GroupMemes memePool, List<Aspect> aspects) {
        this.group = group;
        this.memePool = memePool;
        this.aspectCenter = new AspectCenter(group, aspects);
        this.cultureAspectCenter = new CultureAspectCenter(group, new HashSet<>());
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

    List<Event> getEvents() {
        return events;
    }

    public Set<Group> getRelatedGroups() {
        return relations.keySet();
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

        cultureAspectCenter.getAspectPool().getAspectRequests().forEach(this::addRequest);
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
        aspectCenter.adoptAspects();
        cultureAspectCenter.adoptCultureAspects();
    }

    private void addRequest(Request request) {
        if (request != null) {
            requests.add(request);
        }
    }

    private void removeAspiration(Aspiration aspiration) {
        aspirations.remove(aspiration);
    }

    double getNormalizedRelation(Group group) {
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
            List<Pair<Aspect, Group>> options = aspectCenter.findOptions(aspiration);
            if (options.isEmpty()) {
                return;
            }
            Pair<Aspect, Group> pair = randomElement(options, session.random);
            aspectCenter.addAspect(pair.getFirst());
            removeAspiration(aspiration);
            if (pair.getSecond() == null) {
                group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                        " developed aspect " + pair.getFirst().getName(), "group", this));
            } else {
                group.addEvent(
                        new Event(Event.Type.AspectGaining,
                                String.format(
                                        "Group %s took aspect %s from group %s",
                                        group.name,
                                        pair.getFirst().getName(),
                                        pair.getSecond().name
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

    public void initializeFromCenter(CultureCenter cultureCenter) {//TODO da hell is this?
        cultureCenter.getAspectCenter().getAspectPool().getAll().forEach(a -> aspectCenter.addAspect(a));
    }
}
