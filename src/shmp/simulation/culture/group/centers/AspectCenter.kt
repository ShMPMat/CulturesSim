package shmp.simulation.culture.group.centers

import shmp.random.randomElementOrNull
import shmp.random.singleton.chanceOf
import shmp.random.singleton.chanceOfNot
import shmp.random.singleton.otherwise
import shmp.random.singleton.randomElement
import shmp.simulation.Controller.session
import shmp.simulation.culture.aspect.*
import shmp.simulation.culture.aspect.dependency.AspectDependencies
import shmp.simulation.culture.aspect.dependency.LineDependency
import shmp.simulation.culture.aspect.labeler.ProducedLabeler
import shmp.simulation.culture.group.AspectDependencyCalculator
import shmp.simulation.culture.group.convert
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import java.util.*
import kotlin.math.max


class AspectCenter(aspects: List<Aspect>) {
    private val _mutableAspectPool = MutableAspectPool(HashSet())

    //    Aspects added on the current turn
    private val changedAspectPool = MutableAspectPool(HashSet())
    private val _converseWrappers = mutableSetOf<ConverseWrapper>()
    private val _lastResourcesForCw: MutableList<Resource> = ArrayList()

    init {
        aspects.forEach { hardAspectAdd(it) }
        _mutableAspectPool.all.forEach { it.swapDependencies(this) } //TODO will it swap though?
    }

    val aspectPool: AspectPool
        get() = _mutableAspectPool

    fun addAspect(aspect: Aspect, group: Group): Boolean {
        var currentAspect = aspect
        if (!currentAspect.isValid)
            return false
        if (_mutableAspectPool.contains(currentAspect))
            currentAspect = _mutableAspectPool.getValue(currentAspect.name)
        val dependencies = calculateDependencies(currentAspect, group)
        if (!currentAspect.isDependenciesOk(dependencies))
            return false
        addAspectNow(currentAspect, dependencies, group)
        return true
    }

    private fun addAspectNow(aspect: Aspect, dependencies: AspectDependencies, group: Group) {
        val currentAspect: Aspect
        if (_mutableAspectPool.contains(aspect)) {
            currentAspect = _mutableAspectPool.getValue(aspect) //TODO why one, add a l l
            currentAspect.addOneDependency(dependencies)
        } else {
            currentAspect = aspect.copy(dependencies)
            changedAspectPool.add(currentAspect)
            if (currentAspect !is ConverseWrapper) { //TODO maybe should do the same in straight
                val allResources: MutableSet<Resource> = HashSet(group.overallTerritory.differentResources)
                allResources.addAll(_mutableAspectPool.producedResources)
                for (resource in allResources)
                    addConverseWrapper(currentAspect, resource)
            }
        }
    }

    private fun hardAspectAdd(aspect: Aspect) {
        changedAspectPool.add(aspect)
        _mutableAspectPool.add(aspect)
    }

    private fun calculateDependencies(aspect: Aspect, group: Group): AspectDependencies {
        val calculator = AspectDependencyCalculator(
                _mutableAspectPool,
                group.territoryCenter.territory
        )
        calculator.calculateDependencies(aspect)
        return calculator.dependencies
    }

    private fun addConverseWrapper(aspect: Aspect, resource: Resource) { //TODO I'm adding a lot of garbage
        val wrapper = if (aspect.canApplyMeaning())
            MeaningInserter(aspect, resource)
        else
            ConverseWrapper(aspect, resource)

        if (!wrapper.isValid)
            return
        _converseWrappers.add(wrapper)
    }

    fun mutateAspects(group: Group): List<Aspect> { //TODO separate adding of new aspects and updating old
        (session.rAspectAcquisition / (aspectPool.all.size + 1)).chanceOfNot {
            return listOf()
        }

        val options = mutableListOf<Aspect>()

        if (session.independentCvSimpleAspectAdding) {
            0.1.chanceOf {
                options.addAll(session.world.aspectPool.all)
            } otherwise {
                options.addAll(getAllPossibleConverseWrappers(group))
            }
        } else {
            options.addAll(session.world.aspectPool.all)
            options.addAll(getAllPossibleConverseWrappers(group))
        }

        randomElementOrNull(options, session.random)?.let { aspect ->
            if (aspect is ConverseWrapper && !aspectPool.contains(aspect.aspect))
                return listOf()

            if (addAspect(aspect, group))
                return listOf(aspect)
        }
        return listOf()
    }

    private fun getAllPossibleConverseWrappers(group: Group): List<ConverseWrapper> {
        val options: List<ConverseWrapper> = ArrayList(_converseWrappers) //TODO maybe do it after the middle part?
        val newResources: MutableSet<Resource> = HashSet(group.overallTerritory.differentResources)
        newResources.addAll(_mutableAspectPool.producedResources)
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

    fun findOptions(labeler: ResourceLabeler, group: Group): List<Pair<Aspect, Group?>> {
        val options = mutableListOf<Pair<Aspect, Group?>>()
        val aspectLabeler = ProducedLabeler(labeler)
        for (aspect in session.world.aspectPool.all.filter { aspectLabeler.isSuitable(it) }) {
            val dependencies = calculateDependencies(aspect, group)
            if (aspect.isDependenciesOk(dependencies))
                options.add(aspect.copy(dependencies) to null)
        }
        getAllPossibleConverseWrappers(group)
                .filter { aspectLabeler.isSuitable(it) }
                .forEach { options.add(it to null) }
        val aspects = convert(getNewNeighbourAspects(group))
        for (box in aspects) {
            var aspect = box.aspect
            val aspectGroup = box.group
            if (aspectLabeler.isSuitable(aspect)) {
                val dependencies = calculateDependencies(aspect, group)
                if (aspect.isDependenciesOk(dependencies)) {
                    aspect = aspect.copy(dependencies)
                    options.add(aspect to aspectGroup)
                }
            }
        }
        return options
    }

    private fun getNeighbourAspects(group: Group): List<Pair<Aspect, Group>> {
        val allExistingAspects = mutableListOf<Pair<Aspect, Group>>()
        for (neighbour in group.relationCenter.relatedGroups) {
            allExistingAspects.addAll(
                    neighbour.cultureCenter.aspectCenter._mutableAspectPool.all
                            .filter { (it !is ConverseWrapper || _mutableAspectPool.contains(it.aspect)) }
                            .map { it to neighbour }
            )
        }
        return allExistingAspects
    }

    private fun getNewNeighbourAspects(group: Group): List<Pair<Aspect, Group>> = getNeighbourAspects(group)
            .filter { (a) -> !_mutableAspectPool.contains(a) }

    private fun getNeighbourAspects(group: Group, predicate: (Aspect) -> Boolean) = getNeighbourAspects(group)
            .filter { (a) -> predicate(a) }

    fun adoptAspects(group: Group): List<Event> {
        if (!session.isTime(session.groupTurnsBetweenAdopts))
            return emptyList()

        val allExistingAspects = getNeighbourAspects(group) { !changedAspectPool.contains(it) }
        if (allExistingAspects.isNotEmpty()) {
            val (aspect, aspectGroup) = allExistingAspects.randomElement { (a, g) ->
                max(a.usefulness * group.relationCenter.getNormalizedRelation(g), 0.0) + 1.0
            }

            if (addAspect(aspect, group))
                return listOf(Event(
                        Type.AspectGaining,
                        "${group.name} got ${aspect.name} from ${aspectGroup.name}"
                ))
        }
        return emptyList()
    }

    fun update(crucialAspects: Collection<Aspect>, group: Group) {
        val unimportantAspects = aspectPool.all
                .filter { it.usefulness < session.aspectFalloff }
                .filter { it !in crucialAspects }
        if (unimportantAspects.isEmpty())
            return

        randomElementOrNull(unimportantAspects, session.random)
                ?.takeIf { aspect -> aspect !in aspectPool.converseWrappers.map { it.aspect } }
                ?.let { aspect ->
                    if (_mutableAspectPool.remove(aspect)) {
                        changedAspectPool.deleteDependencyOnAspect(aspect)
                        if (aspect !is ConverseWrapper)
                            _converseWrappers.removeIf { it.aspect == aspect }
                        group.addEvent(Event(Type.Change, "${group.name} lost aspect ${aspect.name}"))
                    }
                }
    }

    override fun toString() = """
        |Aspects:
        |${aspectPool.all.joinToString("\n\n")}
    """.trimMargin()
}
