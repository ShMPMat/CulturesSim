package simulation.culture.group;

import extra.ShnyPair;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.space.resource.ResourceTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.aspect.MeaningInserter;
import simulation.culture.aspect.dependency.*;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.*;
import static shmp.random.RandomProbabilitiesKt.testProbability;
import static simulation.Controller.session;

public class AspectCenter {
    private Group group;
    private Set<Aspect> aspects = new HashSet<>();
    private Set<Aspect> changedAspects = new HashSet<>(); // always equals to aspects except while making a turn

    private List<ConverseWrapper> _converseWrappers = new ArrayList<>();
    private List<Resource> _lastResourcesForCw = new ArrayList<>();

    public AspectCenter(Group group) {
        this.group = group;
    }

    public Set<Aspect> getAspects() {
        return aspects;
    }

    Aspect getAspect(Aspect aspect) {
        return getAspects()
                .stream()
                .filter(aspect::equals)
                .findFirst()
                .orElse(null);
    }

    Aspect getAspect(String name) {
        return getAspects()
                .stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    List<ConverseWrapper> getConverseWrappers() {
        return aspects.stream()
                .filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> (ConverseWrapper) aspect)
                .collect(Collectors.toList());
    }

    Set<ConverseWrapper> getMeaningAspects() {
        return aspects.stream()
                .filter(aspect -> aspect instanceof ConverseWrapper && aspect.canReturnMeaning())
                .map(aspect -> (ConverseWrapper) aspect)
                .collect(Collectors.toSet());
    }

    Set<Aspect> getChangedAspects() {
        return changedAspects;
    }

    List<ShnyPair<Resource, ConverseWrapper>> getAllProducedResources() {
        List<ShnyPair<Resource, ConverseWrapper>> _m = new ArrayList<>();
        for (ConverseWrapper converseWrapper : aspects.stream()
                .filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> (ConverseWrapper) aspect)
                .collect(Collectors.toList())) {
            for (Resource resource : converseWrapper.getResult()) {
                _m.add(new ShnyPair<>(resource, converseWrapper));
            }
        }
        return _m;
    }

    boolean addAspect(Aspect aspect) {
        if (!aspect.isValid()) {
            return false;
        }
        if (aspects.contains(aspect)) {
            aspect = aspects.stream()
                    .filter(aspect::equals)
                    .findFirst()
                    .orElse(aspect);
        }
        Map<ResourceTag, Set<Dependency>> _m = canAddAspect(aspect);
        if (!aspect.isDependenciesOk(_m)) {
            return false;
        }

        addAspectNow(aspect, _m);
        Aspect finalAspect = aspect;
        return true;
    }

    void addAspectNow(Aspect aspect, Map<ResourceTag, Set<Dependency>> dependencies) {
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
            if (!(_a instanceof ConverseWrapper)) {//TODO maybe should do the same in straight
                Set<Resource> allResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
                allResources.addAll(getAllProducedResources().stream().map(pair -> pair.first).collect(Collectors.toSet()));
                for (Resource resource : allResources) {
                    addConverseWrapper(_a, resource);
                }
            }
        }
        group.getCulturalCenter().getMemePool().addAspectMemes(aspect);
        group.getCulturalCenter().addMemeCombination((new MemeSubject(group.name).addPredicate(
                session.world.getPoolMeme("acquireAspect").addPredicate(new MemeSubject(aspect.getName())))));
        neededAdding(_a);
    }

    void hardAspectAdd(Aspect aspect) {
        changedAspects.add(aspect);
        aspects.add(aspect);
        neededAdding(aspect);
    }

    private void neededAdding(Aspect aspect) {
        if (group.getStrata().stream()
                .noneMatch(stratum -> stratum.containsAspect(aspect))) {
            group.getStrata().add(new Stratum(0, aspect, group));
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
                addDependenciesInMap(dep, Collections.singleton(
                        new ConversionDependency(converseWrapper.getRequirement(), group,
                                new ShnyPair<>(converseWrapper.resource, converseWrapper.aspect))), converseWrapper.getRequirement());
            }
            addDependenciesInMap(dep, getAllProducedResources().stream()
                            .filter(pair -> pair.first.equals(converseWrapper.resource))
                            .map(pair -> new LineDependency(converseWrapper.getRequirement(), group,
                                    new ShnyPair<>(converseWrapper, pair.second)))
                            .filter(dependency -> !dependency.isCycleDependency(converseWrapper))
                            .collect(Collectors.toList()),
                    converseWrapper.getRequirement());
        }
    }

    private void addResourceDependencies(ResourceTag requirement, Map<ResourceTag, Set<Dependency>> dep) {
        List<Resource> _r = group.getTerritory().getResourcesWithAspectTag(requirement);
        if (_r != null) {
            addDependenciesInMap(dep, _r.stream()
                    .map(resource -> new ResourceDependency(requirement, group, resource))
                    .collect(Collectors.toList()), requirement);
        }
    }

    private void addAspectDependencies(ResourceTag requirement, Map<ResourceTag, Set<Dependency>> dep, Aspect aspect) {
        for (Aspect selfAspect : getAspects()) {
            if (selfAspect.getTags().contains(requirement)) {
                Dependency dependency = new AspectDependency(requirement, selfAspect);
                if (dependency.isCycleDependency(selfAspect) || dependency.isCycleDependencyInner(aspect)) {
                    continue;
                }
                addDependenciesInMap(dep, Collections.singleton(dependency), requirement);
            }
            addDependenciesInMap(dep, group.getTerritory().getResourcesWhichConverseToTag(selfAspect, requirement).stream() //Make converse Dependency_
                            .map(resource ->
                                    new ConversionDependency(requirement, group, new ShnyPair<>(resource, selfAspect)))
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
            _w = new MeaningInserter(aspect, resource,
                    group);
        } else {
            _w = new ConverseWrapper(aspect, resource,
                    group);
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
        newResources.addAll(getAllProducedResources().stream().map(pair -> pair.first).collect(Collectors.toSet()));
        newResources.removeAll(_lastResourcesForCw);
        for (Aspect aspect : aspects.stream().filter(aspect -> !(aspect instanceof ConverseWrapper))
                .collect(Collectors.toList())) {
            for (Resource resource : newResources) {
                addConverseWrapper(aspect, resource);
            }
        }
        _lastResourcesForCw.addAll(newResources);
        newResources.forEach(resource -> group.getCulturalCenter().getMemePool().addResourceMemes(resource));
        return options;
    }

    void finishUpdate() {
        aspects.forEach(Aspect::finishUpdate);
        pushAspects();
    }

    void pushAspects() {
        aspects = new HashSet<>();
        aspects.addAll(getChangedAspects());
    }

    List<ShnyPair<Aspect, Group>> findOptions(Aspiration aspiration) {
        List<ShnyPair<Aspect, Group>> options = new ArrayList<>();

        for (Aspect aspect : session.world.getAspectPool().getAll().stream().filter(aspiration::isAcceptable).collect(Collectors.toList())) {
            Map<ResourceTag, Set<Dependency>> _m = canAddAspect(aspect);
            if (aspect.isDependenciesOk(_m)) {
                options.add(new ShnyPair<>(aspect.copy(_m, group), null));
            }
        }

        getAllPossibleConverseWrappers().stream().filter(aspiration::isAcceptable)
                .forEach(wrapper -> options.add(new ShnyPair<>(wrapper, null)));

        List<Aspect> aspects = group.getCulturalCenter().getNeighboursAspects();
        for (Aspect aspect : aspects) {
            Map<ResourceTag, Set<Dependency>> _m = canAddAspect(aspect);
            if (aspiration.isAcceptable(aspect) && aspect.isDependenciesOk(_m)) {
                aspect = aspect.copy(_m, group);
                options.add(new ShnyPair<>(aspect, aspect.getGroup()));
            }
        }
        return options;
    }
}
