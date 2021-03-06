package shmp.simulation.culture.group.centers

import shmp.random.singleton.*
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.aspect.*
import shmp.simulation.culture.aspect.dependency.AspectDependencies
import shmp.simulation.culture.aspect.dependency.LineDependency
import shmp.simulation.culture.aspect.labeler.PotentialProducedLabeler
import shmp.simulation.culture.aspect.labeler.ProducedLabeler
import shmp.simulation.culture.group.AspectDependencyCalculator
import shmp.simulation.culture.group.convert
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import shmp.simulation.space.resource.tag.phony
import java.util.*
import kotlin.math.max


class AspectCenter(aspects: List<Aspect>) {
    private val _aspectPool = MutableAspectPool(HashSet())
    val aspectPool: AspectPool = _aspectPool

    //    Aspects added on the current turn
    private val changedAspectPool = MutableAspectPool(HashSet())
    private val _converseWrappers = mutableSetOf<ConverseWrapper>()
    private val _lastResourcesForCw = mutableSetOf<Resource>()

    init {
        aspects.forEach { hardAspectAdd(it) }
        _aspectPool.all.forEach { it.swapDependencies(this) } //TODO will it swap though?
    }

    fun addAspectTry(aspect: Aspect, group: Group): Boolean {
        var currentAspect = aspect
        if (!currentAspect.isValid)
            return false
        if (_aspectPool.contains(currentAspect))
            currentAspect = _aspectPool.getValue(currentAspect.name)
        val dependencies = calculateDependencies(currentAspect, group)
        if (!currentAspect.isDependenciesOk(dependencies))
            return false
        addAspect(currentAspect, dependencies, group)
        return true
    }

    private fun addAspect(aspect: Aspect, dependencies: AspectDependencies, group: Group) {
        val currentAspect: Aspect
        if (_aspectPool.contains(aspect)) {
            currentAspect = _aspectPool.getValue(aspect)
            currentAspect.addOneDependency(dependencies)
        } else {
            currentAspect = aspect.copy(dependencies)
            changedAspectPool.add(currentAspect)
            if (currentAspect !is ConverseWrapper) { //TODO maybe should do the same in straight
                val allResources = AspectDependencyCalculator(_aspectPool, group.overallTerritory)
                        .getAcceptableResources(currentAspect)

                allResources.map { resource -> queueConverseWrapper(currentAspect, resource) }
                        .randomElementOrNull()
                        ?.let { addAspectTry(it, group) }
            }
        }
    }

    private fun hardAspectAdd(aspect: Aspect) {
        changedAspectPool.add(aspect)
        _aspectPool.add(aspect)
    }

    private fun calculateDependencies(aspect: Aspect, group: Group): AspectDependencies {
        val calculator = AspectDependencyCalculator(_aspectPool, group.territoryCenter.territory)
        calculator.calculateDependencies(aspect)
        return calculator.dependencies
    }

    private fun queueConverseWrapper(aspect: Aspect, resource: Resource): ConverseWrapper? {
        val wrapper = if (aspect.canApplyMeaning)
            MeaningInserter(aspect, resource)
        else
            ConverseWrapper(aspect, resource)

        if (!wrapper.isValid)
            return null

        _converseWrappers.add(wrapper)
        return wrapper
    }

    fun mutateAspects(group: Group): List<Aspect> { //TODO separate adding of new aspects and updating old
        (session.rAspectAcquisition / (aspectPool.all.size + 1)).chanceOfNot {
            return listOf()
        }

        val options = mutableListOf<Aspect>()

        if (session.independentCvSimpleAspectAdding) {
            0.1.chanceOf {
                options.addAll(session.world.aspectPool.all.filter { it !in aspectPool.all })
            } otherwise {
                options.addAll(getAllPossibleConverseWrappers(group))
            }
        } else {
            options.addAll(session.world.aspectPool.all)
            options.addAll(getAllPossibleConverseWrappers(group))
        }

        options.randomElementOrNull()?.let { aspect ->
            if (aspect is ConverseWrapper && !aspectPool.contains(aspect.aspect))
                return listOf()

            if (addAspectTry(aspect, group))
                return listOf(aspect)
        }
        return listOf()
    }

    private fun getAllPossibleConverseWrappers(group: Group): List<ConverseWrapper> {
        val newResources = group.overallTerritory.differentResources.filter { it !in _lastResourcesForCw }
                .toMutableSet()
        newResources.addAll(_aspectPool.producedResources.filter { it !in _lastResourcesForCw })

        for (aspect in _aspectPool.filter { it !is ConverseWrapper })
            for (resource in newResources)
                queueConverseWrapper(aspect, resource)
        _lastResourcesForCw.addAll(newResources)
        newResources.forEach { group.cultureCenter.memePool.addResourceMemes(it) }

        return _converseWrappers.sortedBy { it.name }
    }

    fun finishUpdate(): Set<Aspect> {
        _aspectPool.all.forEach { it.finishUpdate() }
        return pushAspects()
    }

    fun pushAspects(): Set<Aspect> {
        changedAspectPool.all.forEach { addNewDependencies(it) }
        _aspectPool.addAll(changedAspectPool.all)
        val addedAspects = changedAspectPool.all
        changedAspectPool.clear()
        return addedAspects
    }

    private fun addNewDependencies(newAspect: Aspect) {
        if (newAspect is ConverseWrapper)
            for (converseWrapper in _aspectPool.converseWrappers) {
                for (tag in newAspect.tags)
                    if (converseWrapper.dependencies.containsDependency(tag))
                        converseWrapper.dependencies.map[tag]!!.add(LineDependency(
                                false,
                                converseWrapper,
                                newAspect
                        ))
                if (newAspect.producedResources.any { converseWrapper.resource == it })
                    converseWrapper.dependencies.map.getValue(phony).add(LineDependency(
                            true,
                            converseWrapper,
                            newAspect
                    ))
            }
    }

    fun findOptions(labeler: ResourceLabeler, group: Group): List<Pair<Aspect, Group?>> {
        val options = mutableListOf<Pair<Aspect, Group?>>()
        val aspectLabeler = ProducedLabeler(labeler)
        0.5.chanceOf {
            val aspectPotentialLabeler = PotentialProducedLabeler(labeler, session.world.resourcePool)
            for (aspect in session.world.aspectPool.all.filter { aspectPotentialLabeler.isSuitable(it) }) {
                val dependencies = calculateDependencies(aspect, group)
                if (aspect.isDependenciesOk(dependencies))
                    options.add(aspect.copy(dependencies) to null)
            }
        }

        getAllPossibleConverseWrappers(group)
                .filter { aspectLabeler.isSuitable(it) }
                .forEach { options.add(it to null) }

        val aspects = convert(getNewNeighbourAspects(group))
        for ((aspect, aspectGroup) in aspects) {
            if (aspectLabeler.isSuitable(aspect)) {
                val dependencies = calculateDependencies(aspect, group)
                if (aspect.isDependenciesOk(dependencies))
                    options.add(aspect.copy(dependencies) to aspectGroup)
            }
        }
        return options
    }

    private fun getNeighbourAspects(group: Group): List<Pair<Aspect, Group>> {
        val allExistingAspects = mutableListOf<Pair<Aspect, Group>>()
        for (neighbour in group.relationCenter.relatedGroups) {
            allExistingAspects.addAll(
                    neighbour.cultureCenter.aspectCenter._aspectPool.all
                            .filter { (it !is ConverseWrapper || _aspectPool.contains(it.aspect)) }
                            .map { it to neighbour }
            )
        }
        return allExistingAspects
    }

    private fun getNewNeighbourAspects(group: Group): List<Pair<Aspect, Group>> = getNeighbourAspects(group)
            .filter { (a) -> !_aspectPool.contains(a) }

    private fun getNeighbourAspects(group: Group, predicate: (Aspect) -> Boolean) = getNeighbourAspects(group)
            .filter { (a) -> predicate(a) }

    fun adoptAspects(group: Group): List<Event> {
        session.groupAspectAdoptionProb.chanceOfNot {
            return emptyList()
        }

        val allExistingAspects = getNeighbourAspects(group) { !changedAspectPool.contains(it) }
        if (allExistingAspects.isNotEmpty()) {
            val (aspect, aspectGroup) = allExistingAspects.randomElement { (a, g) ->
                max(a.usefulness * group.relationCenter.getNormalizedRelation(g), 0.0) + 1.0
            }

            if (addAspectTry(aspect, group))
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

        unimportantAspects.randomElementOrNull()
                ?.takeIf { aspect -> aspect !in aspectPool.converseWrappers.map { it.aspect } }
                ?.let { aspect ->
                    if (aspect in changedAspectPool.converseWrappers.map { it.aspect })
                        return@let

                    if (_aspectPool.remove(aspect)) {
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
