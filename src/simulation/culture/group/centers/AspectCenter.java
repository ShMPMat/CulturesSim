package simulation.culture.group.centers;

import kotlin.Pair;
import shmp.random.RandomException;
import simulation.Event;
import simulation.culture.aspect.*;
import simulation.culture.aspect.MutableAspectPool;
import simulation.culture.group.AspectDependencyCalculator;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.dependency.*;
import simulation.space.resource.Resource;
import simulation.space.resource.tag.labeler.ResourceLabeler;

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

    public boolean addAspect(Aspect aspect) {
        if (!aspect.isValid()) {
            return false;
        }
        if (aspectPool.contains(aspect)) {
            aspect = aspectPool.getValue(aspect.getName());
        }
        AspectDependencies _m = calculateDependencies(aspect);
        if (!aspect.isDependenciesOk(_m)) {
            return false;
        }

        addAspectNow(aspect, _m);
        return true;
    }

    private void addAspectNow(Aspect aspect, AspectDependencies dependencies) {
        Aspect _a;
        if (aspectPool.contains(aspect)) {
            _a = aspectPool.getValue(aspect);//TODO why one, add a l l
            _a.addOneDependency(dependencies);
        } else {
            _a = aspect.copy(dependencies);
            changedAspectPool.add(_a);
            if (!(_a instanceof ConverseWrapper)) {//TODO maybe should do the same in straight
                Set<Resource> allResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
                allResources.addAll(aspectPool.getProducedResources().stream()
                        .map(Pair::getFirst)
                        .collect(Collectors.toSet()));
                for (Resource resource : allResources) {
                    addConverseWrapper(_a, resource);
                }
            }
        }
    }

    void hardAspectAdd(Aspect aspect) {
        changedAspectPool.add(aspect);
        aspectPool.add(aspect);
    }

    AspectDependencies calculateDependencies(Aspect aspect) {
        AspectDependencyCalculator calculator = new AspectDependencyCalculator(
                aspectPool,
                group.getTerritoryCenter().getTerritory()
        );
        calculator.calculateDependencies(aspect);
        return calculator.getDependencies();
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

    Collection<Event> mutateAspects() { //TODO separate adding of new aspects and updating old
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
                    return Collections.singleton(new Event(
                                    Event.Type.AspectGaining,
                                    String.format("Got aspect %s by itself", _a.getName())
                            ));
                }
            }
        }
        return new ArrayList<>();
    }

    private List<ConverseWrapper> getAllPossibleConverseWrappers() {
        List<ConverseWrapper> options = new ArrayList<>(_converseWrappers); //TODO maybe do it after the middle part?
        Set<Resource> newResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
        newResources.addAll(aspectPool.getProducedResources().stream()
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
        changedAspectPool.getAll().forEach(this::addNewDependencies);
        aspectPool.addAll(changedAspectPool.getAll());
        Set<Aspect> addedAspects = changedAspectPool.getAll();
        changedAspectPool.clear();
        return addedAspects;
    }

    private void addNewDependencies(Aspect newAspect) {
        if (newAspect instanceof ConverseWrapper) {
            for (ConverseWrapper converseWrapper : aspectPool.getConverseWrappers()) {
                for (ResourceTag tag : newAspect.getTags()) {
                    if (converseWrapper.getDependencies().containsDependency(tag)) {
                        converseWrapper.getDependencies().getMap().get(tag).add(new LineDependency(
                                false,
                                converseWrapper,
                                (ConverseWrapper) newAspect
                                ));
                    }
                }
                if (newAspect.getProducedResources().stream().anyMatch(r -> converseWrapper.resource.equals(r))) {
                    converseWrapper.getDependencies().getMap().get(ResourceTag.phony()).add(new LineDependency(
                            true,
                            converseWrapper,
                            (ConverseWrapper) newAspect
                    ));
                }
            }
        }
    }

    List<Pair<Aspect, Group>> findOptions(ResourceLabeler labeler) {
        List<Pair<Aspect, Group>> options = new ArrayList<>();
        AspectLabeler aspectLabeler = new AspectLabeler(labeler);
        for (Aspect aspect : session.world.getAspectPool().getAll().stream()
                .filter(aspectLabeler::isSuitable)
                .collect(Collectors.toList())) {
            AspectDependencies _m = calculateDependencies(aspect);
            if (aspect.isDependenciesOk(_m)) {
                options.add(new Pair<>(aspect.copy(_m), null));
            }
        }

        getAllPossibleConverseWrappers().stream().filter(aspectLabeler::isSuitable)
                .forEach(wrapper -> options.add(new Pair<>(wrapper, null)));

        List<Pair<Aspect, Group>> aspects = getNeighbourAspects();
        for (Pair<Aspect, Group> pair : aspects) {
            Aspect aspect = pair.getFirst();
            Group aspectGroup = pair.getSecond();
            AspectDependencies _m = calculateDependencies(aspect);
            if (aspectLabeler.isSuitable(aspect) && aspect.isDependenciesOk(_m)) {
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

    Collection<Event> adoptAspects(Group group) {
        if (!session.isTime(session.groupTurnsBetweenAdopts)) {
            return new ArrayList<>();
        }
        List<Pair<Aspect, Group>> allExistingAspects = getNeighbourAspects(a -> !changedAspectPool.contains(a));

        if (!allExistingAspects.isEmpty()) {
            try {
                Pair<Aspect, Group> pair = randomElement(
                        allExistingAspects,
                        p -> p.getFirst().getUsefulness() * group.getRelationCenter().getNormalizedRelation(p.getSecond()),
                        session.random
                );
                if (addAspect(pair.getFirst())) {
                    return Collections.singleton(new Event(
                                    Event.Type.AspectGaining,
                                    String.format(
                                            "Got aspect %s from group %s",
                                            pair.getFirst().getName(), pair.getSecond().name)
                            ));
                }
            } catch (Exception e) {
                if (e instanceof RandomException) {
                    int i = 0;//TODO
                }
            }
        }
        return  new ArrayList<>();
    }
}