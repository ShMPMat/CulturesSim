package simulation.culture.group;

import extra.ProbFunc;
import extra.ShnyPair;
import simulation.culture.Event;
import simulation.culture.aspect.*;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static simulation.Controller.*;

/**
 * Takes responsibility of Group's cultural change.
 */
public class CulturalCenter {
    private List<Aspiration> aspirations = new ArrayList<>();
    private Group group;
    private Set<Aspect> aspects = new HashSet<>();
    private Set<Aspect> changedAspects = new HashSet<>(); // always equals aspects except while making a turn
    private List<Event> events = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();
    private List<ShnyPair<Resource, ResourceBehaviour>> wants = new ArrayList<>();
    private GroupMemes memePool = new GroupMemes();

    private List<ConverseWrapper> _converseWrappers = new ArrayList<>();
    private List<Resource> _lastResourcesForCw = new ArrayList<>();

    CulturalCenter(Group group) {
        this.group = group;
    }

    List<Aspiration> getAspirations() {
        return aspirations;
    }

    Set<Aspect> getAspects() {
        return aspects;
    }

    Aspect getAspect(Aspect aspect) {
        return getAspects().stream().filter(aspect::equals).findFirst().orElse(null);
    }

    List<Request> getRequests() {
        return requests;
    }

    Set<Aspect> getChangedAspects() {
        return changedAspects;
    }

    List<Event> getEvents() {
        return events;
    }

    private Set<ShnyPair<Aspect, Group>> getNeighboursAspects() {
        Set<ShnyPair<Aspect, Group>> allExistingAspects = new HashSet<>();
        for (Group neighbour : sessionController.world.map.getAllNearGroups(group)) {
            for (Aspect aspect : neighbour.getAspects().stream().filter(aspect ->
                    !(aspect instanceof ConverseWrapper) || getAspect(((ConverseWrapper) aspect).aspect) != null)
                    .collect(Collectors.toList())) {
                allExistingAspects.add(new ShnyPair<>(aspect, neighbour));
            }
        }
        return allExistingAspects;
    }

    public List<ShnyPair<Resource, ConverseWrapper>> getAllProducedResources() {
        List<ShnyPair<Resource, ConverseWrapper>> _m = new ArrayList<>();
        for (ConverseWrapper converseWrapper : aspects.stream().filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> (ConverseWrapper) aspect).collect(Collectors.toList())) {
            for (Resource resource : converseWrapper.getResult()) {
                _m.add(new ShnyPair<>(resource, converseWrapper));
            }
        }
        return _m;
    }

    GroupMemes getMemePool() {
        return memePool;
    }

    void addMemeCombination(Meme meme) {
        memePool.addMemeCombination(meme);
    }

    void addAspiration(Aspiration aspiration) { //TODO seems that aspirations are stuck in groups;
        if (aspirations.stream().noneMatch(aspir -> aspir.equals(aspiration))) {
            aspirations.add(aspiration);
        }
        if (group.getParentGroup() != null) {
            group.getParentGroup().getCulturalCenter().addAspiration(aspiration);
        }
    }

    public boolean addAspect(Aspect aspect) {
        if (aspects.contains(aspect)) {
            aspect = aspects.stream().filter(aspect::equals).findFirst().orElse(aspect);
        }
        Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(aspect);
        if (!aspect.isDependenciesOk(_m)) {
            return false;
        }

        addAspectNow(aspect, _m);
        Aspect finalAspect = aspect;
        group.subgroups.forEach(group -> group.getCulturalCenter().addAspectNow(finalAspect, _m));
        return true;
    }

    void addAspectNow(Aspect aspect, Map<AspectTag, Set<Dependency>> dependencies) {
        Aspect _a = null;
        if (getChangedAspects().contains(aspect)) {
            for (Aspect as : getChangedAspects()) {
                if (as.equals(aspect)) {
                    as.addOneDependency(dependencies);
                    _a = as;
                }
            }
        } else {
            _a = aspect.copy(dependencies, group);
            getChangedAspects().add(_a);
            if (!(_a instanceof ConverseWrapper)) {
                Set<Resource> allResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
                allResources.addAll(getAllProducedResources().stream().map(pair -> pair.first).collect(Collectors.toSet()));
                for (Resource resource : allResources) {
                    addConverseWrapper(_a, resource);
                }
            }
        }
        memePool.addAspectMemes(aspect);
        addMemeCombination((new MemeSubject(group.name).addPredicate(
                sessionController.world.getMemeFromPoolByName("acquireAspect").addPredicate(new MemeSubject(aspect.getName())))));
        if (_a instanceof ConverseWrapper) {
            addWants(((ConverseWrapper) _a).getResult());
        }
        Aspect final_a = _a;
        if (group.getStrata().stream().noneMatch(stratum -> stratum.containsAspect(final_a))) {
            group.getStrata().add(new Stratum(0, _a));
        }
    }

    void addWants(List<Resource> resources) {
        resources.forEach(this::addWantWithProbability);
    }

    void addWantWithProbability(Resource resource) {
        if (ProbFunc.getChances(0.1)) {
            addWant(resource);
        }
    }

    public void addWant(Resource resource) {
        if (wants.stream().noneMatch(pair -> pair.first.equals(resource))) {
            wants.add(new ShnyPair<>(resource, new ResourceBehaviour(new PlacementStrategy(group.getOverallTerritory(),
                            ProbFunc.randomElement(PlacementStrategy.Strategy.values())))));
        }
    }

    void updateRequests() {
        requests = new ArrayList<>();
        int foodFloor = group.population / group.getFertility() + 1;
        BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> foodPenalty = (pair, percent) -> {
            pair.first.starve(percent);
            pair.second.destroyAllResourcesWithTag(new AspectTag("food"));
            return null;
        };
        BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> foodReward = (pair, percent) -> {
            pair.first.population += ((int) (percent * pair.first.population)) / 10 + 1;
            pair.first.population = Math.min(pair.first.population, group.getMaxPopulation());
            pair.second.destroyAllResourcesWithTag(new AspectTag("food"));
            return null;
        };
        requests.add(new Request(group, new AspectTag("food"), foodFloor,
                foodFloor + group.population / 100 + 1, foodPenalty, foodReward));

        if (group.getTerritory().getMinTemperature() < 0) {
            BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> warmthPenalty = (pair, percent) -> {
                pair.first.freeze(percent);
                return null;
            };
            BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> warmthReward = (pair, percent) -> {
                return null;
            };
            requests.add(new Request(group, new AspectTag("warmth"), group.population,
                    group.population, warmthPenalty, warmthReward));
        }

        for (ShnyPair<Resource, ResourceBehaviour> want : wants) {
            requests.add(new Request(group, want.first, 1, 10, (pair, percent) -> {
                pair.first.cherishedResources.add(pair.second);
                addAspiration(new Aspiration(5, want.first));
                want.second.procedeResources(pair.second);
                return null;
            },
                    (pair, percent) -> {
                pair.first.cherishedResources.add(pair.second);
                want.second.procedeResources(pair.second);
                return null;
                    }));
        }
    }

    void overgroupUpdate() {//TODO move aspects to subgroups and share them between subgroups
        tryToFulfillAspirations();
        mutateAspects();
    }

    void update() {
        createArtifact();
    }

    private void removeAspiration(Aspiration aspiration) {
        aspirations.remove(aspiration);
    }

    private void mutateAspects() { //TODO separate adding of new aspects und updating old

//        Set<ShnyPair<Aspect, Group>> allExistingAspects = getNeighboursAspects();
//
//        ShnyPair<Aspect, Group> _p = ProbFunc.addRandomAspectWithPairExcept(getChangedAspects(),
//                allExistingAspects, pair -> !getChangedAspects().contains(pair.first), rAspectLending);
//        if (_p != null) {
//            if (addAspect(_p.first)) {
//                group.addEvent(new Event(Event.Type.AspectGaining, world.getTurn(), "Group " + group.name +
//                        " got aspect " + _p.first.getBaseName() + " from group " + _p.second.name, "group", group));
//            }
//        }
        if (ProbFunc.getChances(sessionController.rAspectAcquisition)) {
            List<Aspect> options = new ArrayList<>(sessionController.world.aspectPool);
            options.addAll(getAllConverseWrappers());
            Aspect _a = ProbFunc.randomElement(options, aspect -> true);
            if (_a != null) {
                if (addAspect(_a)) {
                    group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                            " got aspect " + _a.getName() + " by itself", "group", group));
                }
            }
        }
    }

    private void createArtifact() {
        if (ProbFunc.getChances(0.1)) {
            if (memePool.isEmpty()){
                return;
            }
            List<ConverseWrapper> _l = aspects.stream()
                    .filter(aspect -> aspect instanceof ConverseWrapper && aspect.canReturnMeaning())
                    .map(aspect -> (ConverseWrapper) aspect).collect(Collectors.toList());
            ConverseWrapper _a = ProbFunc.randomElement(_l);
            if (_a == null) {
                return;
            }
            ConverseWrapper _b = _a.stripToMeaning();
            ShnyPair<Boolean, ResourcePack> _p = _b.use(1, new ResourceEvaluator(rp -> rp, ResourcePack::getAmount));
            for (Resource resource : _p.second.resources) {
                if (resource.hasMeaning()) {
                    group.uniqueArtifacts.add(resource);
                } else {
                    group.resourcePack.add(resource);
                }
            }
        }
    }

    private void tryToFulfillAspirations() {
        Optional _o = getAspirations().stream().max((Comparator.comparingInt(o -> o.level)));
        if (_o.isPresent()) {
            Aspiration aspiration = (Aspiration) _o.get();
            List<ShnyPair<Aspect, Group>> options = findOptions(aspiration);
            ShnyPair<Aspect, Group> pair = ProbFunc.randomElement(options);
            if (pair == null) {
                return;
            }
            addAspect(pair.first);
            removeAspiration(aspiration);
            if (pair.second == null) {
                group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                        " developed aspect " + pair.first.getName(), "group", this));
            } else {
                group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                        " took aspect " + pair.first.getName() + " from group " + pair.second.name, "group", this));
            }
        }
    }

    private List<ShnyPair<Aspect, Group>> findOptions(Aspiration aspiration) {//TODO add potentially good options (incinerate for warmth etc.)
        List<ShnyPair<Aspect, Group>> options = new ArrayList<>();

        for (Aspect aspect : sessionController.world.getAllDefaultAspects().stream().filter(aspiration::isAcceptable).collect(Collectors.toList())) {
            Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(aspect);
            if (aspect.isDependenciesOk(_m)) {
                options.add(new ShnyPair<>(aspect.copy(_m, group), null));
            }
        }

        getAllConverseWrappers().stream().filter(aspiration::isAcceptable)
                .forEach(wrapper -> options.add(new ShnyPair<>(wrapper, null)));

        Set<ShnyPair<Aspect, Group>> aspects = getNeighboursAspects();
        for (ShnyPair<Aspect, Group> pair : aspects) {
            Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(pair.first);
            if (aspiration.isAcceptable(pair.first) && pair.first.isDependenciesOk(_m)) {
                pair.first = pair.first.copy(_m, group);
                options.add(pair);
            }
        }
        return options;
    }

    private List<ConverseWrapper> getAllConverseWrappers() {
        List<ConverseWrapper> options = new ArrayList<>(_converseWrappers);
        Set<Resource> newResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
        newResources.addAll(getAllProducedResources().stream().map(pair -> pair.first).collect(Collectors.toSet()));
        newResources.removeAll(_lastResourcesForCw);
        for (Aspect aspect : aspects.stream().filter(aspect -> !(aspect instanceof ConverseWrapper))
                .collect(Collectors.toList())) {
            for (Resource resource : newResources) {
                addConverseWrapper(aspect, resource);
            }
        }
        _lastResourcesForCw.addAll(newResources);
        newResources.forEach(resource -> getMemePool().add(new MemeSubject(resource.getBaseName())));
        return options;
    }

    private void addConverseWrapper(Aspect aspect, Resource resource) {
        ConverseWrapper _w;
        if (aspect.canApplyMeaning()) {
            _w = new MeaningInserter(aspect, resource,
                    group);
        } else {
            _w = new ConverseWrapper(aspect, resource,
                    group);
        }
        Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(_w);
        if (_w.isDependenciesOk(_m)) {
            _converseWrappers.add(_w);
        }
    }

    void finishUpdate() {
        pushAspects();
    }

    public void pushAspects() {
        aspects = new HashSet<>();
        getAspects().addAll(getChangedAspects());
    }

    public Meme getMeaning() {
        return memePool.getValuableMeme();
    }

    public Collection<ShnyPair<Resource, ResourceBehaviour>> getWants() {
        return wants;
    }
}
