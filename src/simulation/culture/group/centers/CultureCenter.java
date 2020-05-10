package simulation.culture.group.centers;

import kotlin.Pair;
import simulation.Controller;
import simulation.Event;
import simulation.culture.aspect.*;
import simulation.culture.group.GroupTileTagKt;
import simulation.culture.group.cultureaspect.AestheticallyPleasingObject;
import simulation.culture.group.request.*;
import simulation.culture.group.resource_behaviour.ResourceBehaviourKt;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.Territory;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.Resource;
import simulation.space.resource.tag.ResourceTag;
import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.resource.tag.labeler.TagLabeler;
import simulation.space.tile.TileTag;

import java.util.*;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.randomElement;
import static shmp.random.RandomProbabilitiesKt.testProbability;
import static simulation.Controller.session;
import static simulation.culture.group.GroupsKt.*;

public class CultureCenter {
    private AspectCenter aspectCenter;
    private CultureAspectCenter cultureAspectCenter;
    private Group group;
    private List<Event> events = new ArrayList<>();
    private GroupMemes memePool;
    private RequestPool turnRequests = new RequestPool(new HashMap<>());

    CultureCenter(Group group, GroupMemes memePool, List<Aspect> aspects) {
        this.group = group;
        this.memePool = memePool;
        this.aspectCenter = new AspectCenter(group, aspects);
        this.cultureAspectCenter = new CultureAspectCenter(group);
    }

    public AspectCenter getAspectCenter() {
        return aspectCenter;
    }

    public CultureAspectCenter getCultureAspectCenter() {
        return cultureAspectCenter;
    }

    public RequestPool getTurnRequests() {
        return turnRequests;
    }

    void updateRequests(int foodFloor) {
        Map<Request, MutableResourcePack> map = new HashMap<>();
        for (Request request : _updateRequests(foodFloor)) {
            map.put(request, new MutableResourcePack());
        }
        turnRequests = new RequestPool(map);
    }

    List<Event> getEvents() {
        return events;
    }

    public GroupMemes getMemePool() {
        return memePool;
    }

    public void addAspiration(ResourceLabeler labeler) {
        group.getResourceCenter().addNeeded(labeler, 100);
    }

    public void addResourceWant(Resource resource) {
        cultureAspectCenter.addCultureAspect(new AestheticallyPleasingObject(
                resource,
                ResourceBehaviourKt.getRandom(group, Controller.session.random)
        ));
    }

    private List<Request> _updateRequests(int foodFloor) {
        List<Request> requests = new ArrayList<>();
        Request foodRequest = new TagRequest(
                group,
                new ResourceTag("food"),
                foodFloor,
                foodFloor + group.getPopulationCenter().getPopulation() / 100 + 1,
                getFoodPenalty(),
                getFoodReward()
        );
        requests.add(foodRequest);

        if (group.getTerritoryCenter().getTerritory().getMinTemperature() < 0) {
            requests.add(new TagRequest(
                    group,
                    new ResourceTag("warmth"),
                    group.getPopulationCenter().getPopulation(),
                    group.getPopulationCenter().getPopulation(),
                    getWarmthPenalty(),
                    getPassingReward()
            ));
        }

        if (turnRequests.getResultStatus().containsKey(foodRequest) &&
                turnRequests.getResultStatus().get(foodRequest).getStatus() != ResultStatus.NotSatisfied) {
            ResourceEvaluator clothesEvaluator = EvaluatorsKt.tagEvaluator(new ResourceTag("clothes"));
            int neededClothes = group.getPopulationCenter().getPopulation() -
                    (int) clothesEvaluator.evaluate(group.getResourceCenter().getPack());
            if (neededClothes > 0) {
                requests.add(new TagRequest(
                        group,
                        new ResourceTag("clothes"),
                        neededClothes,
                        neededClothes,
                        unite(List.of(
                                addNeed(new TagLabeler(new ResourceTag("clothes"))),
                                put()
                        )),
                        put()
                ));
            } else if (group.getPopulationCenter().getPopulation() > 0) {
                int h = 0;
            }

            ResourceEvaluator shelterEvaluator = EvaluatorsKt.tagEvaluator(new ResourceTag("shelter"));
            int neededShelter = group.getPopulationCenter().getPopulation() -
                    (int) shelterEvaluator.evaluate(group.getResourceCenter().getPack());
            if (neededShelter > 0) {
                requests.add(new TagRequest(
                        group,
                        new ResourceTag("shelter"),
                        neededClothes,
                        neededClothes,
                        unite(List.of(
                                addNeed(new TagLabeler(new ResourceTag("shelter"))),
                                put()
                        )),
                        put()
                ));
            } else if (group.getPopulationCenter().getPopulation() > 0) {
                int h = 0;
            }
        }
        cultureAspectCenter.getAspectPool().getAspectRequests(group).stream()
                .filter(Objects::nonNull)
                .forEach(requests::add);
        return requests;
    }

    void update() {
        events.addAll(aspectCenter.mutateAspects());
        aspectCenter.update(cultureAspectCenter.getAspectPool().getCwDependencies());
        createArtifact();
        cultureAspectCenter.useCultureAspects();
        cultureAspectCenter.addRandomCultureAspect(group);
        cultureAspectCenter.mutateCultureAspects(group);
        lookOnTerritory(group.getTerritoryCenter().getAccessibleTerritory());
    }

    void lookOnTerritory(Territory accessibleTerritory) {
        List<TileTag> tags = accessibleTerritory.getTiles().stream()
                .flatMap(t -> t.getTagPool().getAll().stream())
                .collect(Collectors.toList());
        for (TileTag tag : tags) {
            if (tag.getType().equals(GroupTileTagKt.GROUP_TAG_TYPE)) {
                continue;
            }
            memePool.add(new MemeSubject(tag.getName()));
            memePool.strengthenMeme(new MemeSubject(tag.getName()));
        }
    }

    void intergroupUpdate() {
        events.addAll(aspectCenter.adoptAspects(group));
        cultureAspectCenter.adoptCultureAspects(group);
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
                    1,
                    EvaluatorsKt.getPassingEvaluator(),
                    group.getPopulationCenter(),
                    group.getTerritoryCenter().getAccessibleTerritory(),
                    true,
                    group,
                    memePool.getValuableMeme()
            ));
            for (Resource resource : result.resources.getResources()) {
                if (resource.hasMeaning()) {
                    group.getResourceCenter().add(resource);
                }
            }
        }
    }

    public Meme getMeaning() {
        return memePool.getValuableMeme();
    }

    void addNeedAspect(Pair<ResourceLabeler, ResourceNeed> need) {
        List<Pair<Aspect, Group>> options = aspectCenter.findOptions(need.getFirst());
        if (options.isEmpty()) {
            return;
        }
        Pair<Aspect, Group> pair = randomElement(options, session.random);
        aspectCenter.addAspect(pair.getFirst());
        if (pair.getSecond() == null) {
            events.add(new Event(
                    Event.Type.AspectGaining,
                    "Group " + group.name + " developed aspect " + pair.getFirst().getName(),
                    "group", this
            ));
        } else {
            events.add(new Event(
                    Event.Type.AspectGaining,
                    String.format(
                            "Group %s took aspect %s from group %s",
                            group.name,
                            pair.getFirst().getName(),
                            pair.getSecond().name
                    ),
                    "group", this
            ));
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

    public void die() {
        cultureAspectCenter.die(group);
    }
}
