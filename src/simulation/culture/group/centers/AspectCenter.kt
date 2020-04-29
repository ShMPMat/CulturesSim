package simulation.culture.group.centers

import shmp.random.RandomException
import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.culture.aspect.*
import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.aspect.dependency.LineDependency
import simulation.culture.group.AspectDependencyCalculator
import simulation.culture.group.convert
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.ResourceLabeler
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

class AspectCenter(private val group: Group, aspects: List<Aspect?>) {
    private val aspectPool = MutableAspectPool(HashSet())

    /**
     * Equals to aspects added on the current turn
     */
    private val changedAspectPool = MutableAspectPool(HashSet())
    private val _converseWrappers: MutableList<ConverseWrapper> = ArrayList()
    private val _lastResourcesForCw: MutableList<Resource> = ArrayList()
    fun getAspectPool(): AspectPool {
        return aspectPool
    }

    fun addAspect(aspect: Aspect): Boolean {
        var aspect = aspect
        if (!aspect.isValid) {
            return false
        }
        if (aspectPool.contains(aspect)) {
            aspect = aspectPool.getValue(aspect.name)
        }
        val _m = calculateDependencies(aspect)
        if (!aspect.isDependenciesOk(_m)) {
            return false
        }
        addAspectNow(aspect, _m)
        return true
    }

    private fun addAspectNow(aspect: Aspect, dependencies: AspectDependencies) {
        val _a: Aspect
        if (aspectPool.contains(aspect)) {
            _a = aspectPool.getValue(aspect) //TODO why one, add a l l
            _a.addOneDependency(dependencies)
        } else {
            _a = aspect.copy(dependencies)
            changedAspectPool.add(_a)
            if (_a !is ConverseWrapper) { //TODO maybe should do the same in straight
                val allResources: MutableSet<Resource> = HashSet(group.overallTerritory.differentResources)
                allResources.addAll(aspectPool.producedResources.stream()
                        .map(::kotlin.Pair.first)
                        .collect(Collectors.toSet()))
                for (resource in allResources) {
                    addConverseWrapper(_a, resource)
                }
            }
        }
    }

    fun hardAspectAdd(aspect: Aspect?) {
        changedAspectPool.add(aspect!!)
        aspectPool.add(aspect)
    }

    fun calculateDependencies(aspect: Aspect?): AspectDependencies {
        val calculator = AspectDependencyCalculator(
                aspectPool,
                group.territoryCenter.territory
        )
        calculator.calculateDependencies(aspect!!)
        return calculator.dependencies
    }

    private fun addConverseWrapper(aspect: Aspect, resource: Resource) { //TODO I'm adding a lot of garbage
        val _w: ConverseWrapper
        _w = if (aspect.canApplyMeaning()) {
            MeaningInserter(aspect, resource)
        } else {
            ConverseWrapper(aspect, resource)
        }
        if (!_w.isValid) {
            return
        }
        _converseWrappers.add(_w)
    }

    fun mutateAspects(): Collection<Event> { //TODO separate adding of new aspects and updating old
        if (testProbability(Controller.session.rAspectAcquisition, Controller.session.random)) {
            val options: MutableList<Aspect> = ArrayList()
            if (Controller.session.independentCvSimpleAspectAdding) {
                if (testProbability(0.1, Controller.session.random)) {
                    options.addAll(Controller.session.world.aspectPool.getAll())
                } else {
                    options.addAll(allPossibleConverseWrappers)
                }
            } else {
                options.addAll(Controller.session.world.aspectPool.getAll())
                options.addAll(allPossibleConverseWrappers)
            }
            if (!options.isEmpty()) {
                val _a = randomElement(options, Controller.session.random)
                if (addAspect(_a)) {
                    return setOf(Event(
                            Event.Type.AspectGaining, String.format("Got aspect %s by itself", _a.name)))
                }
            }
        }
        return ArrayList()
    }

    //TODO maybe do it after the middle part?
    private val allPossibleConverseWrappers: List<ConverseWrapper>
        private get() {
            val options: List<ConverseWrapper> = ArrayList(_converseWrappers) //TODO maybe do it after the middle part?
            val newResources: MutableSet<Resource> = HashSet(group.overallTerritory.differentResources)
            newResources.addAll(aspectPool.producedResources.stream()
                    .map(::kotlin.Pair.first)
                    .collect(Collectors.toSet()))
            newResources.removeAll(_lastResourcesForCw)
            for (aspect in aspectPool.filter { aspect: Aspect? -> aspect !is ConverseWrapper }) {
                for (resource in newResources) {
                    addConverseWrapper(aspect, resource)
                }
            }
            _lastResourcesForCw.addAll(newResources)
            newResources.forEach(Consumer { resource: Resource? -> group.cultureCenter.memePool.addResourceMemes(resource) })
            return options
        }

    fun finishUpdate(): Set<Aspect> {
        aspectPool.getAll().forEach(Consumer { obj: Aspect -> obj.finishUpdate() })
        return pushAspects()
    }

    fun pushAspects(): Set<Aspect> {
        changedAspectPool.getAll().forEach(Consumer { newAspect: Aspect -> addNewDependencies(newAspect) })
        aspectPool.addAll(changedAspectPool.getAll())
        val addedAspects = changedAspectPool.getAll()
        changedAspectPool.clear()
        return addedAspects
    }

    private fun addNewDependencies(newAspect: Aspect) {
        if (newAspect is ConverseWrapper) {
            for (converseWrapper in aspectPool.converseWrappers) {
                for (tag in newAspect.tags) {
                    if (converseWrapper.dependencies.containsDependency(tag)) {
                        converseWrapper.dependencies.map[tag]!!.add(LineDependency(
                                false,
                                converseWrapper,
                                newAspect
                        ))
                    }
                }
                if (newAspect.producedResources.stream().anyMatch { r: Resource -> converseWrapper.resource == r }) {
                    converseWrapper.dependencies.map[ResourceTag.phony()]!!.add(LineDependency(
                            true,
                            converseWrapper,
                            newAspect
                    ))
                }
            }
        }
    }

    fun findOptions(labeler: ResourceLabeler): List<Pair<Aspect, Group?>> {
        if (labeler.toString().contains("clothes")) {
            val h = 0
        }
        val options: MutableList<Pair<Aspect, Group?>> = ArrayList()
        val aspectLabeler = AspectLabeler(labeler)
        for (aspect in Controller.session.world.aspectPool.getAll().stream()
                .filter { aspect: Aspect? -> aspectLabeler.isSuitable(aspect!!) }
                .collect(Collectors.toList())) {
            val _m = calculateDependencies(aspect)
            if (aspect.isDependenciesOk(_m)) {
                options.add(Pair<Aspect, Group?>(aspect.copy(_m), null))
            }
        }
        allPossibleConverseWrappers.stream().filter { aspect: ConverseWrapper? -> aspectLabeler.isSuitable(aspect!!) }
                .forEach { wrapper: ConverseWrapper -> options.add(Pair<Aspect, Group?>(wrapper, null)) }
        val aspects = convert(newNeighbourAspects)
        for (box in aspects) {
            var aspect = box.aspect
            val aspectGroup = box.group
            if (aspectLabeler.isSuitable(aspect)) {
                val _m = calculateDependencies(aspect)
                if (aspect.isDependenciesOk(_m)) {
                    aspect = aspect.copy(_m)
                    options.add(Pair(aspect, aspectGroup))
                }
            }
        }
        return options
    }

    val neighbourAspects: List<Pair<Aspect, Group>>
        get() {
            val allExistingAspects: MutableList<Pair<Aspect, Group>> = ArrayList()
            for (neighbour in group.relationCenter.relatedGroups) {
                allExistingAspects.addAll(
                        neighbour.cultureCenter.aspectCenter.aspectPool.getAll().stream()
                                .filter { aspect: Aspect? ->
                                    (aspect !is ConverseWrapper
                                            || aspectPool.contains(aspect.aspect))
                                }
                                .map { a: Aspect -> Pair(a, neighbour) }
                                .collect(Collectors.toList()))
            }
            return allExistingAspects
        }

    val newNeighbourAspects: List<Pair<Aspect, Group>>
        get() = neighbourAspects.stream()
                .filter { (first) -> !aspectPool.contains(first) }
                .collect(Collectors.toList())

    fun getNeighbourAspects(predicate: Predicate<Aspect?>): List<Pair<Aspect, Group>> {
        return neighbourAspects.stream()
                .filter { (first) -> predicate.test(first) }
                .collect(Collectors.toList())
    }

    fun adoptAspects(group: Group): Collection<Event> {
        if (!Controller.session.isTime(Controller.session.groupTurnsBetweenAdopts)) {
            return ArrayList()
        }
        val allExistingAspects = getNeighbourAspects(Predicate { a: Aspect? -> !changedAspectPool.contains(a!!) })
        if (!allExistingAspects.isEmpty()) {
            try {
                val (first1, second1) = randomElement(
                        allExistingAspects,
                        { (first, second) -> first.usefulness * group.relationCenter.getNormalizedRelation(second) },
                        Controller.session.random
                )
                if (addAspect(first1)) {
                    return setOf(Event(
                            Event.Type.AspectGaining, String.format(
                            "Got aspect %s from group %s",
                            first1.name, second1.name)))
                }
            } catch (e: Exception) {
                if (e is RandomException) {
                    val i = 0 //TODO
                }
            }
        }
        return ArrayList()
    }

    fun update() {}

    init {
        aspects.forEach(Consumer { aspect: Aspect? -> hardAspectAdd(aspect) })
        aspectPool.getAll().forEach(Consumer { a: Aspect -> a.swapDependencies(this) }) //TODO will it swap though?
    }
}