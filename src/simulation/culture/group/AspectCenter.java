package simulation.culture.group;

import kotlin.Pair;
import shmp.random.RandomException;
import simulation.Event;
import simulation.culture.aspect.*;
import simulation.culture.aspect.MutableAspectPool;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.dependency.*;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.*;
import static shmp.random.RandomProbabilitiesKt.testProbability;
import static simulation.Controller.session;

public class AspectCenter {
    private Group group;
    private MutableAspectPool aspectPool = new MutableAspectPool(new HashSet<>());
    /**
     * Equals to aspects added on the current turn
     */
    private MutableAspectPool changedAspectPool = new MutableAspectPool(new HashSet<>());

    private List<ConverseWrapper> _converseWrappers = new ArrayList<>();
    private List<Resource> _lastResourcesForCw = new ArrayList<>();

    public AspectCenter(Group group, List<Aspect> aspects) {
        this.group = group;
        aspects.forEach(this::hardAspectAdd);
        aspectPool.getAll().forEach(a -> a.swapDependencies(this));//TODO will it swap though?
    }

    public AspectPool getAspectPool() {
        return aspectPool;
    }

    public List<Pair<Resource, ConverseWrapper>> getAllProducedResources() {
        List<Pair<Resource, ConverseWrapper>> _m = new ArrayList<>();
        for (ConverseWrapper converseWrapper : aspectPool.getConverseWrappers()) {
            for (Resource resource : converseWrapper.getResult()) {
                _m.add(new Pair<>(resource, converseWrapper));
            }
        }
        return _m;
    }

    public boolean addAspect(Aspect aspect) {
        if (!aspect.isValid()) {
            return false;
        }
        if (aspectPool.contains(aspect)) {
            aspect = aspectPool.get(aspect.getName());
        }
        Map<ResourceTag, Set<Dependency>> _m = canAddAspect(aspect);
        if (!aspect.isDependenciesOk(_m)) {
            return false;
        }

        addAspectNow(aspect, _m);
        return true;
    }

    private void addAspectNow(Aspect aspect, Map<ResourceTag, Set<Dependency>> dependencies) {
        Aspect _a;
        if (aspectPool.contains(aspect)) {
            _a = aspectPool.get(aspect);//TODO why one, add a l l
            _a.addOneDependency(dependencies);
        } else {
            _a = aspect.copy(dependencies);
            changedAspectPool.add(_a);
            if (!(_a instanceof ConverseWrapper)) {//TODO maybe should do the same in straight
                Set<Resource> allResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
                allResources.addAll(getAllProducedResources().stream()
                        .map(Pair::getFirst)
                        .collect(Collectors.toSet()));
                for (Resource resource : allResources) {
                    addConverseWrapper(_a, resource);
                }
            }
        }
        neededAdding(_a);
    }

    void hardAspectAdd(Aspect aspect) {
        aspectPool.add(aspect);
        neededAdding(aspect);
    }

    private void neededAdding(Aspect aspect) {
        if (group.getPopulationCenter().getStrata().stream()
                .noneMatch(stratum -> stratum.containsAspect(aspect))) {
            group.getPopulationCenter().getStrata().add(new Stratum(0, aspect, group));
        }
    }


    Map<ResourceTag, Set<Dependency>> canAddAspect(Aspect aspect) {
        Map<ResourceTag, Set<Dependency>> dep = new HashMap<>();
        if (aspect instanceof ConverseWrapper) {
            addForConverseWrapper((ConverseWrapper) aspect, dep);
        }
        for (ResourceTag requirement : aspect.getRequirements()) {
            if (requirement.name.equals(ResourceTag.phony().name) || requirement.isWrapperCondition()) {
                continue;
            }
            addAspectDependencies(requirement, dep, aspect);
        }
        return dep;
    }

    private void addForConverseWrapper(ConverseWrapper converseWrapper, Map<ResourceTag, Set<Dependency>> dep) {
        if (converseWrapper.resource.hasApplicationForAspect(converseWrapper.aspect)) {
            if (converseWrapper.canTakeResources() && group.getOverallTerritory().getDifferentResources().contains(converseWrapper.resource)) {
                addDependenciesInMap(
                        dep,
                        Collections.singleton(
                                new ConversionDependency(
                                        converseWrapper.getRequirement(),
                                        new Pair<>(converseWrapper.resource, converseWrapper.aspect)
                                )),
                        converseWrapper.getRequirement()
                );
            }
            addDependenciesInMap(dep, getAllProducedResources().stream()
                            .filter(pair -> pair.getFirst().equals(converseWrapper.resource))
                            .map(pair -> new LineDependency(
                                    converseWrapper.getRequirement(),
                                    new Pair<>(converseWrapper, pair.getSecond())
                            )).filter(dependency -> !dependency.isCycleDependency(converseWrapper))
                            .collect(Collectors.toList()),
                    converseWrapper.getRequirement());
        }
    }

    private void addAspectDependencies(ResourceTag requirement, Map<ResourceTag, Set<Dependency>> dep, Aspect aspect) {
        for (Aspect selfAspect : aspectPool.getAll()) {
            if (selfAspect.getTags().contains(requirement)) {
                Dependency dependency = new AspectDependency(requirement, selfAspect);
                if (dependency.isCycleDependency(selfAspect) || dependency.isCycleDependencyInner(aspect)) {
                    continue;
                }
                addDependenciesInMap(dep, Collections.singleton(dependency), requirement);
            }
            addDependenciesInMap(dep, group.getTerritoryCenter().getTerritory().getResourcesWhichConverseToTag(
                    selfAspect,
                    requirement
                    ).stream() //Make converse Dependency_
                            .map(resource ->
                                    new ConversionDependency(requirement, new Pair<>(resource, selfAspect)))
                            .filter(dependency -> !dependency.isCycleDependency(aspect))
                            .collect(Collectors.toList()),
                    requirement);
        }
    }

    private void addDependenciesInMap(Map<ResourceTag, Set<Dependency>> dep, Collection<Dependency> dependencies,
                                      ResourceTag requirement) {
        if (dependencies.isEmpty()) {
            return;
        }
        if (!dep.containsKey(requirement)) {
            dep.put(requirement, new HashSet<>());
        }
        dep.get(requirement).addAll(dependencies);
    }

    private void addConverseWrapper(Aspect aspect, Resource resource) { //TODO I'm adding a lot of garbage
        ConverseWrapper _w;
        if (aspect.canApplyMeaning()) {
            _w = new MeaningInserter(aspect, resource);
        } else {
            _w = new ConverseWrapper(aspect, resource);
        }
        if (!_w.isValid()) {
            return;
        }
        _converseWrappers.add(_w);
    }

    void mutateAspects() { //TODO separate adding of new aspects and updating old
        if (testProbability(session.rAspectAcquisition, session.random)) {
            List<Aspect> options = new ArrayList<>();
            if (session.independentCvSimpleAspectAdding) {
                if (testProbability(0.1, session.random)) {
                    options.addAll(session.world.getAspectPool().getAll());
                } else {
                    options.addAll(getAllPossibleConverseWrappers());
                }
            } else {
                options.addAll(session.world.getAspectPool().getAll());
                options.addAll(getAllPossibleConverseWrappers());
            }

            if (!options.isEmpty()) {
                Aspect _a = randomElement(options, session.random);
                if (addAspect(_a)) {
                    group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                            " got aspect " + _a.getName() + " by itself", "group", group));
                }
            }
        }
    }

    private List<ConverseWrapper> getAllPossibleConverseWrappers() {
        List<ConverseWrapper> options = new ArrayList<>(_converseWrappers); //TODO maybe do it after the middle part?
        Set<Resource> newResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
        newResources.addAll(getAllProducedResources().stream()
                .map(Pair::getFirst)
                .collect(Collectors.toSet()));
        newResources.removeAll(_lastResourcesForCw);
        for (Aspect aspect : aspectPool.filter(aspect -> !(aspect instanceof ConverseWrapper))) {
            for (Resource resource : newResources) {
                addConverseWrapper(aspect, resource);
            }
        }
        _lastResourcesForCw.addAll(newResources);
        newResources.forEach(resource -> group.getCultureCenter().getMemePool().addResourceMemes(resource));
        return options;
    }

    Set<Aspect> finishUpdate() {
        aspectPool.getAll().forEach(Aspect::finishUpdate);
        return pushAspects();
    }

    Set<Aspect> pushAspects() {
        aspectPool.addAll(changedAspectPool.getAll());
        Set<Aspect> addedAspects = changedAspectPool.getAll();
        changedAspectPool.clear();
        return addedAspects;
    }

    List<Pair<Aspect, Group>> findOptions(Aspiration aspiration) {
        List<Pair<Aspect, Group>> options = new ArrayList<>();

        for (Aspect aspect : session.world.getAspectPool().getAll().stream()
                .filter(aspiration::isAcceptable)
                .collect(Collectors.toList())) {
            Map<ResourceTag, Set<Dependency>> _m = canAddAspect(aspect);
            if (aspect.isDependenciesOk(_m)) {
                options.add(new Pair<>(aspect.copy(_m), null));
            }
        }

        getAllPossibleConverseWrappers().stream().filter(aspiration::isAcceptable)
                .forEach(wrapper -> options.add(new Pair<>(wrapper, null)));

        List<Pair<Aspect, Group>> aspects = getNeighbourAspects();
        for (Pair<Aspect, Group> pair : aspects) {
            Aspect aspect = pair.getFirst();
            Group aspectGroup = pair.getSecond();
            Map<ResourceTag, Set<Dependency>> _m = canAddAspect(aspect);
            if (aspiration.isAcceptable(aspect) && aspect.isDependenciesOk(_m)) {
                aspect = aspect.copy(_m);
                options.add(new Pair<>(aspect, aspectGroup));
            }
        }
        return options;
    }

    List<Pair<Aspect, Group>> getNeighbourAspects() {
        List<Pair<Aspect, Group>> allExistingAspects = new ArrayList<>();
        for (Group neighbour : group.getRelationCenter().getRelatedGroups()) {
            allExistingAspects.addAll(neighbour.getCultureCenter().getAspectCenter().getAspectPool().getAll().stream()
                    .filter(aspect -> !(aspect instanceof ConverseWrapper)
                            || aspectPool.contains(((ConverseWrapper) aspect).aspect))
                    .map(a -> new Pair<>(a, neighbour))
                    .collect(Collectors.toList()));
        }
        return allExistingAspects;
    }

    List<Pair<Aspect, Group>> getNeighbourAspects(Predicate<Aspect> predicate) {
        return getNeighbourAspects().stream()
                .filter(p -> predicate.test(p.getFirst()))
                .collect(Collectors.toList());
    }

    void adoptAspects() {
        if (!session.isTime(session.groupTurnsBetweenAdopts)) {
            return;
        }
        List<Pair<Aspect, Group>> allExistingAspects = getNeighbourAspects(a -> !changedAspectPool.contains(a));

        if (!allExistingAspects.isEmpty()) {
            try {
                Pair<Aspect, Group> pair = randomElementWithProbability(
                        allExistingAspects,
                        p -> p.getFirst().getUsefulness() * group.getRelationCenter().getNormalizedRelation(p.getSecond()),
                        session.random
                );
                if (addAspect(pair.getFirst())) {
                    group.addEvent(new Event(Event.Type.AspectGaining,
                            String.format("Group %s got aspect %s from group %s",
                                    group.name, pair.getFirst().getName(), pair.getSecond().name),
                            "group", group));
                }
            } catch (Exception e) {
                if (e instanceof RandomException) {
                    int i = 0;//TODO
                }
            }
        }
    }
}
