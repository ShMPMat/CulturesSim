package simulation.culture.group;

import kotlin.Pair;
import simulation.Controller;
import simulation.Event;
import simulation.culture.aspect.*;
import simulation.culture.group.cultureaspect.*;
import simulation.culture.group.request.EvaluatorsKt;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.group.request.TagRequest;
import simulation.culture.group.resource_behaviour.ResourceBehaviourKt;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.function.BiFunction;

import static shmp.random.RandomProbabilitiesKt.testProbability;
import static shmp.random.RandomCollectionsKt.*;
import static simulation.Controller.*;
import static simulation.culture.group.GroupEffectFunctionsKt.freeze;
import static simulation.culture.group.GroupEffectFunctionsKt.starve;

public class CultureCenter {
    private AspectCenter aspectCenter;
    private CultureAspectCenter cultureAspectCenter;
    private List<Aspiration> aspirations = new ArrayList<>();
    private Group group;
    private List<Event> events = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();
    private GroupMemes memePool;

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

    public GroupMemes getMemePool() {
        return memePool;
    }

    void addAspiration(Aspiration aspiration) {
        if (aspirations.stream().noneMatch(aspir -> aspir.equals(aspiration))) {
            aspirations.add(aspiration);
        }
    }

    public void addResourceWant(Resource resource) {
        cultureAspectCenter.addCultureAspect(new AestheticallyPleasingObject(
                resource,
                ResourceBehaviourKt.getRandom(group, Controller.session.random)
        ));
    }

    void updateRequests(int foodFloor) {
        requests = new ArrayList<>();
        BiFunction<Pair<Group, MutableResourcePack>, Double, Void> foodPenalty = (pair, percent) -> {
            starve(pair.getFirst(), percent);
            pair.getSecond().destroyAllResourcesWithTag(new ResourceTag("food"));
            return null;
        };
        BiFunction<Pair<Group, MutableResourcePack>, Double, Void> foodReward = (pair, percent) -> {
            pair.getFirst().getPopulationCenter().goodConditionsGrow(percent);
            pair.getSecond().destroyAllResourcesWithTag(new ResourceTag("food"));
            return null;
        };
        requests.add(new TagRequest(group, new ResourceTag("food"), foodFloor,
                foodFloor + group.getPopulationCenter().getPopulation() / 100 + 1, foodPenalty, foodReward));

        if (group.getTerritoryCenter().getTerritory().getMinTemperature() < 0) {
            BiFunction<Pair<Group, MutableResourcePack>, Double, Void> warmthPenalty = (pair, percent) -> {
                freeze(pair.getFirst(), percent);
                return null;
            };
            BiFunction<Pair<Group, MutableResourcePack>, Double, Void> warmthReward = (pair, percent) -> null;
            requests.add(new TagRequest(
                    group,
                    new ResourceTag("warmth"),
                    group.getPopulationCenter().getPopulation(),
                    group.getPopulationCenter().getPopulation(),
                    warmthPenalty,
                    warmthReward
            ));
        }

        cultureAspectCenter.getAspectPool().getAspectRequests(group).forEach(this::addRequest);
    }

    void update() {
        tryToFulfillAspirations();
        events.addAll(aspectCenter.mutateAspects());
        createArtifact();
        cultureAspectCenter.useCultureAspects();
        cultureAspectCenter.addRandomCultureAspect(group);
        cultureAspectCenter.mutateCultureAspects();
    }

    void intergroupUpdate() {
        events.addAll(aspectCenter.adoptAspects(group));
        cultureAspectCenter.adoptCultureAspects(group);
    }

    private void addRequest(Request request) {
        if (request != null) {
            requests.add(request);
        }
    }

    private void removeAspiration(Aspiration aspiration) {
        aspirations.remove(aspiration);
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
            AspectResult result = _a.use(new AspectController(
                    1,
                    1,
                    EvaluatorsKt.getPassingEvaluator(),
                    group.getPopulationCenter(),
                    group.getTerritoryCenter().getAccessibleTerritory(),
                    true,
                    memePool.getValuableMeme()
            ));
            for (Resource resource : result.resources.getResources()) {
                if (resource.hasMeaning()) {
                    group.uniqueArtifacts.add(resource);
                }//TODO do something otherwise
            }
        }
    }

    public Meme getMeaning() {
        return memePool.getValuableMeme();
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
                events.add(new Event(Event.Type.AspectGaining, "Group " + group.name +
                        " developed aspect " + pair.getFirst().getName(), "group", this));
            } else {
                events.add(
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

    Set<Aspect> finishAspectUpdate() {
        Set<Aspect> aspects = aspectCenter.finishUpdate();
        aspects.forEach(a -> {
            memePool.addAspectMemes(a);
            memePool.addMemeCombination((new MemeSubject(group.name).addPredicate(
                    session.world.getPoolMeme("acquireAspect").addPredicate(new MemeSubject(a.getName())))));
        });
        return aspects;
    }

    public void pushAspects() {
        aspectCenter.pushAspects();
    }
}
