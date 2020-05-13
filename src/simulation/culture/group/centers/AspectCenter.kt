package simulation.culture.group.centers

import shmp.random.RandomException
import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.*
import simulation.Event
import simulation.culture.aspect.*
import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.aspect.dependency.LineDependency
import simulation.culture.aspect.labeler.ProducedLabeler
import simulation.culture.group.AspectDependencyCalculator
import simulation.culture.group.GroupError
import simulation.culture.group.convert
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.ResourceLabeler
import java.util.*
import java.util.function.Predicate

class AspectCenter(private val group: Group, aspects: List<Aspect>) {
    private val _mutableAspectPool = MutableAspectPool(HashSet())


    /**
     * Equals to aspects added on the current turn
     */
    private val changedAspectPool = MutableAspectPool(HashSet())
    private val _converseWrappers = mutableSetOf<ConverseWrapper>()
    private val _lastResourcesForCw: MutableList<Resource> = ArrayList()

    init {
        aspects.forEach { hardAspectAdd(it) }
        _mutableAspectPool.all.forEach { it.swapDependencies(this) } //TODO will it swap though?
    }

    val aspectPool: AspectPool
        get() = _mutableAspectPool

    fun addAspect(aspect: Aspect): Boolean {
        var currentAspect = aspect
        if (!currentAspect.isValid)
            return false
        if (_mutableAspectPool.contains(currentAspect))
            currentAspect = _mutableAspectPool.getValue(currentAspect.name)
        val dependencies = calculateDependencies(currentAspect)
        if (!currentAspect.isDependenciesOk(dependencies))
            return false
        addAspectNow(currentAspect, dependencies)
        return true
    }

    private fun addAspectNow(aspect: Aspect, dependencies: AspectDependencies) {
        val currentAspect: Aspect
        if (_mutableAspectPool.contains(aspect)) {
            currentAspect = _mutableAspectPool.getValue(aspect) //TODO why one, add a l l
            currentAspect.addOneDependency(dependencies)
        } else {
            currentAspect = aspect.copy(dependencies)
            changedAspectPool.add(currentAspect)
            if (currentAspect !is ConverseWrapper) { //TODO maybe should do the same in straight
                val allResources: MutableSet<Resource> = HashSet(group.overallTerritory.differentResources)
                allResources.addAll(_mutableAspectPool.producedResources.map { it.first })
                for (resource in allResources)
                    addConverseWrapper(currentAspect, resource)
            }
        }
    }

    private fun hardAspectAdd(aspect: Aspect) {
        changedAspectPool.add(aspect)
        _mutableAspectPool.add(aspect)
    }

    private fun calculateDependencies(aspect: Aspect): AspectDependencies {
        val calculator = AspectDependencyCalculator(
                _mutableAspectPool,
                group.territoryCenter.territory
        )
        calculator.calculateDependencies(aspect)
        return calculator.dependencies
    }

    private fun addConverseWrapper(aspect: Aspect, resource: Resource) { //TODO I'm adding a lot of garbage
        val wrapper = (if (aspect.canApplyMeaning())
            MeaningInserter(aspect, resource)
        else
            ConverseWrapper(aspect, resource))
        if (!wrapper.isValid)
            return
        _converseWrappers.add(wrapper)
    }

    fun mutateAspects(): Collection<Event> { //TODO separate adding of new aspects and updating old
        if (testProbability(session.rAspectAcquisition, session.random)) {
            val options: MutableList<Aspect> = ArrayList()
            if (session.independentCvSimpleAspectAdding) {
                if (testProbability(0.1, session.random))
                    options.addAll(session.world.aspectPool.all)
                else
                    options.addAll(allPossibleConverseWrappers)
            } else {
                options.addAll(session.world.aspectPool.all)
                options.addAll(allPossibleConverseWrappers)
            }
            if (options.isNotEmpty()) {
                val aspect = randomElement(options, session.random)
                if (aspect is ConverseWrapper && !aspectPool.contains(aspect.aspect)) {
                    return listOf()
                }
                if (addAspect(aspect)) {
                    return setOf(Event(
                            Event.Type.AspectGaining, String.format("Got aspect %s by itself", aspect.name)
                    ))
                }
            }
        }
        return listOf()
    }

    private val allPossibleConverseWrappers: List<ConverseWrapper>
        get() {
            val options: List<ConverseWrapper> = ArrayList(_converseWrappers) //TODO maybe do it after the middle part?
            val newResources: MutableSet<Resource> = HashSet(group.overallTerritory.differentResources)
            newResources.addAll(_mutableAspectPool.producedResources.map { it.first })
            newResources.removeAll(_lastResourcesForCw)
            for (aspect in _mutableAspectPool.filter { it !is ConverseWrapper })
                for (resource in newResources)
                    addConverseWrapper(aspect, resource)
            _lastResourcesForCw.addAll(newResources)
            newResources.forEach { group.cultureCenter.memePool.addResourceMemes(it) }
            return options
        }

    fun finishUpdate(): Set<Aspect> {
        _mutableAspectPool.all.forEach { it.finishUpdate() }
        return pushAspects()
    }

    fun pushAspects(): Set<Aspect> {
        changedAspectPool.all.forEach { addNewDependencies(it) }
        _mutableAspectPool.addAll(changedAspectPool.all)
        val addedAspects = changedAspectPool.all
        changedAspectPool.clear()
        return addedAspects
    }

    private fun addNewDependencies(newAspect: Aspect) {
        if (newAspect is ConverseWrapper)
            for (converseWrapper in _mutableAspectPool.converseWrappers) {
                for (tag in newAspect.tags)
                    if (converseWrapper.dependencies.containsDependency(tag))
                        converseWrapper.dependencies.map[tag]!!.add(LineDependency(
                                false,
                                converseWrapper,
                                newAspect
                        ))
                if (newAspect.producedResources.any { converseWrapper.resource == it })
                    converseWrapper.dependencies.map[ResourceTag.phony()]!!.add(LineDependency(
                            true,
                            converseWrapper,
                            newAspect
                    ))
            }
    }

    fun findOptions(labeler: ResourceLabeler): List<Pair<Aspect, Group?>> {
        val options: MutableList<Pair<Aspect, Group?>> = ArrayList()
        val aspectLabeler = ProducedLabeler(labeler)
        for (aspect in session.world.aspectPool.all.filter { aspectLabeler.isSuitable(it) }) {
            val dependencies = calculateDependencies(aspect)
            if (aspect.isDependenciesOk(dependencies))
                options.add(Pair<Aspect, Group?>(aspect.copy(dependencies), null))
        }
        allPossibleConverseWrappers
                .filter { aspectLabeler.isSuitable(it) }
                .forEach { options.add(Pair<Aspect, Group?>(it, null)) }
        val aspects = convert(newNeighbourAspects)
        for (box in aspects) {
            var aspect = box.aspect
            val aspectGroup = box.group
            if (aspectLabeler.isSuitable(aspect)) {
                val dependencies = calculateDependencies(aspect)
                if (aspect.isDependenciesOk(dependencies)) {
                    aspect = aspect.copy(dependencies)
                    options.add(Pair(aspect, aspectGroup))
                }
            }
        }
        return options
    }

    private val neighbourAspects: List<Pair<Aspect, Group>>
        get() {
            val allExistingAspects: MutableList<Pair<Aspect, Group>> = ArrayList()
            for (neighbour in group.relationCenter.relatedGroups) {
                allExistingAspects.addAll(
                        neighbour.cultureCenter.aspectCenter._mutableAspectPool.all
                                .filter { (it !is ConverseWrapper || _mutableAspectPool.contains(it.aspect)) }
                                .map { Pair(it, neighbour) }
                )
            }
            return allExistingAspects
        }

    private val newNeighbourAspects: List<Pair<Aspect, Group>>
        get() = neighbourAspects.filter { (a) -> !_mutableAspectPool.contains(a) }

    private fun getNeighbourAspects(predicate: Predicate<Aspect>) =
            neighbourAspects.filter { (a) -> predicate.test(a) }

    fun adoptAspects(group: Group): Collection<Event> {
        if (!session.isTime(session.groupTurnsBetweenAdopts))
            return ArrayList()
        val allExistingAspects = getNeighbourAspects(Predicate { !changedAspectPool.contains(it) })
        if (allExistingAspects.isNotEmpty()) try {
            val (aspect, aspectGroup) = randomElement(
                    allExistingAspects,
                    { (a, g) -> a.usefulness * group.relationCenter.getNormalizedRelation(g) },
                    session.random
            )
            if (addAspect(aspect)) return setOf(Event(
                    Event.Type.AspectGaining, String.format(
                    "Got aspect %s from group %s",
                    aspect.name, aspectGroup.name)))
        } catch (e: Exception) {
            if (e is RandomException) {
                val i = 0 //TODO
            }
        }
        return ArrayList()
    }

    fun update(crucialAspects: Collection<Aspect>) {
        val unimportantAspects = aspectPool.all
                .filter { it.usefulness < session.aspectFalloff }
                .filter { it !in crucialAspects }
        if (unimportantAspects.isEmpty()) return
        val aspect = randomElement(unimportantAspects, session.random)
        if (aspect !in aspectPool.converseWrappers.map { it.aspect }) {
            if (_mutableAspectPool.remove(aspect) && aspect !is ConverseWrapper) {
                _converseWrappers.removeIf { it.aspect == aspect }
            }
        }
    }
}