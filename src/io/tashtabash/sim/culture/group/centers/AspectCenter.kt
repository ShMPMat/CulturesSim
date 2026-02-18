package io.tashtabash.sim.culture.group.centers

import io.tashtabash.random.singleton.*
import io.tashtabash.random.toSampleSpaceObject
import io.tashtabash.sim.culture.aspect.*
import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.culture.aspect.dependency.AspectDependency
import io.tashtabash.sim.culture.aspect.dependency.LineDependency
import io.tashtabash.sim.culture.aspect.labeler.ProducedLabeler
import io.tashtabash.sim.culture.group.AspectDependencyCalculator
import io.tashtabash.sim.culture.group.convert
import io.tashtabash.sim.culture.group.request.tagEvaluator
import io.tashtabash.sim.event.*
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.sim.space.resource.tag.mainDependencyName
import java.util.*
import kotlin.math.max


class AspectCenter(aspects: List<Aspect> = listOf()) {
    private val _aspectPool = MutableAspectPool(HashSet())
    val aspectPool: AspectPool = _aspectPool

    //    Aspects added on the current turn
    val changedAspectPool = MutableAspectPool(HashSet())
    private val _potentialConverseWrappers = mutableSetOf<ConverseWrapper>()
    private val _lastResourcesForCw = mutableSetOf<Resource>()

    init {
        aspects.forEach { hardAspectAdd(it) }
        _aspectPool.all.forEach { it.swapDependencies(this) } //TODO will it swap though?
    }

    fun tryAddingAspect(aspect: Aspect, group: Group): Boolean {
        var currentAspect = aspect
        if (!currentAspect.isValid)
            return false
        if (_aspectPool.contains(currentAspect))
            currentAspect = _aspectPool.getValue(currentAspect.name)
        val (dependencies) = calculateDependencies(currentAspect, group)
        if (!currentAspect.checkDependencies(dependencies))
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
                val allResources = AspectDependencyCalculator(_aspectPool, group.overallTerritory, changedAspectPool)
                        .getAcceptableResources(currentAspect)

                allResources.map { resource -> queueConverseWrapper(currentAspect, resource) }
                        .randomElementOrNull()
                        ?.let { tryAddingAspect(it, group) }
            }
        }
    }

    private fun hardAspectAdd(aspect: Aspect) {
        changedAspectPool.add(aspect)
        _aspectPool.add(aspect)
    }

    private fun calculateDependencies(
        aspect: Aspect,
        group: Group
    ): Pair<AspectDependencies, MutableCollection<Pair<ResourceNeed, ResourceTag>>> {
        val calculator = AspectDependencyCalculator(_aspectPool, group.territoryCenter.territory, changedAspectPool)
        calculator.calculateDependencies(aspect)
        return calculator.dependencies to calculator.needs.values
    }

    private fun queueConverseWrapper(aspect: Aspect, resource: Resource): ConverseWrapper? {
        val wrapper = if (aspect.canApplyMeaning)
            MeaningInserter(aspect, resource)
        else
            ConverseWrapper(aspect, resource)

        if (!wrapper.isValid)
            return null

        _potentialConverseWrappers.add(wrapper)
        return wrapper
    }

    fun getAllPossibleConverseWrappers(group: Group): List<ConverseWrapper> {
        val resources = getAllPossibleResources(group)

        for (aspect in _aspectPool.filter { it !is ConverseWrapper })
            for (resource in resources)
                queueConverseWrapper(aspect, resource)
        _lastResourcesForCw.addAll(resources)
        resources.forEach { group.cultureCenter.memePool.addResourceMemes(it) }

        return _potentialConverseWrappers.sortedBy { it.name }
    }

    private fun getAllPossibleResources(group: Group): Set<Resource> {
        val resources = group.overallTerritory.differentResources.filter { it !in _lastResourcesForCw }
                .toMutableSet()
        resources.addAll(_aspectPool.producedResources.filter { it !in _lastResourcesForCw })

        return resources
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
                        converseWrapper.dependencies.map[tag]!!.add(AspectDependency(
                                false,
                                newAspect,
                                tagEvaluator(tag),
                                converseWrapper,
                        ))
                if (newAspect.producedResources.any { converseWrapper.resource == it })
                    converseWrapper.dependencies.map.getValue(mainDependencyName).add(LineDependency(
                            true,
                            newAspect,
                            converseWrapper,
                    ))
            }
    }

    private fun findPossibleAspectOptions(labeler: ResourceLabeler, group: Group): Map<Int, List<AspectOption>> {
        val allOptions = mutableListOf<Pair<ConverseWrapper, Group?>>()
        val aspectLabeler = ProducedLabeler(labeler)

        getAllPossibleConverseWrappers(group)
            .filter { aspectLabeler.isSuitable(it) }
            .forEach { allOptions += it to null }
        val aspects = convert(getNewNeighbourAspects(group))
        for ((aspect, aspectGroup) in aspects)
            if (aspect is ConverseWrapper && aspectLabeler.isSuitable(aspect))
                allOptions += aspect to aspectGroup

        return allOptions
            .map { (aspect, sourceGroup) ->
                val (dependencies, needs) = calculateDependencies(aspect, group)

                AspectOption(aspect, dependencies, sourceGroup, needs)
            }.groupBy { it.needs.size }
    }

    private fun insertDependency(aspect: ConverseWrapper, tag: ResourceTag, dependencyWrapper: ConverseWrapper) {
        aspect.dependencies.map[tag] = mutableSetOf(
            if (tag == mainDependencyName)
                LineDependency(true, dependencyWrapper, aspect)
            else
                AspectDependency(false, dependencyWrapper, tagEvaluator(tag), aspect)
        )
    }

    // Returns Aspects which, when added, produce a Resource satisfying the labeler
    // If an aspect has dependencies in the list, they are placed at later indices
    fun findRandomOption(labeler: ResourceLabeler, group: Group, depth: Int = 1): List<SourcedAspect> {
        val possibleOptions = findPossibleAspectOptions(labeler, group)

        possibleOptions[0]
            ?.filter { (aspect, dependencies) -> aspect.checkDependencies(dependencies) }
            ?.randomElementOrNull()
            ?.let { (aspect, dependencies, sourceGroup) ->
                return listOf(aspect.copy(dependencies) to sourceGroup)
            }

        if (depth < 2)
            return emptyList()

        possibleOptions.entries
            .flatMap { (n, options) -> options.map { it.toSampleSpaceObject(1.0 / n * n) } }
            .randomUnwrappedElementOrNull()
            ?.let { (aspect, _, sourceGroup, needs) ->
                val resultOptions = mutableListOf(aspect to sourceGroup)
                for ((need, tag) in needs) {
                    val needOption = findRandomOption(need.resourceLabeler, group, depth - 1)
                    if (needOption.isNotEmpty()) {
                        resultOptions += needOption
                        insertDependency(aspect, tag, needOption[0].first)
                    } else
                        return emptyList()
                }
                return resultOptions
            }
        return emptyList()
    }

    private fun getNeighbourAspects(group: Group): List<Pair<Aspect, Group>> {
        val allExistingAspects = mutableListOf<Pair<Aspect, Group>>()
        for (neighbour in group.relationCenter.relatedGroups) {
            allExistingAspects += neighbour.cultureCenter.aspectCenter._aspectPool.all
                .filter { (it !is ConverseWrapper || _aspectPool.contains(it.aspect)) }
                .map { it to neighbour }
        }
        return allExistingAspects
    }

    private fun getNewNeighbourAspects(group: Group): List<Pair<Aspect, Group>> = getNeighbourAspects(group)
            .filter { (a) -> !_aspectPool.contains(a) }

    private fun getNeighbourAspects(group: Group, predicate: (Aspect) -> Boolean) = getNeighbourAspects(group)
            .filter { (a) -> predicate(a) }

    fun adoptAspects(group: Group): List<Event> {
        val allExistingAspects = getNeighbourAspects(group) { !changedAspectPool.contains(it) }
        if (allExistingAspects.isNotEmpty()) {
            val (aspect, aspectGroup) = allExistingAspects.randomElement { (a, g) ->
                max(a.usefulness * group.relationCenter.getNormalizedRelation(g), 0.0) + 1.0
            }

            if (tryAddingAspect(aspect, group))
                return listOf(AspectGaining of "${group.name} got ${aspect.name} from ${aspectGroup.name}")
        }
        return emptyList()
    }

    fun remove(aspect: Aspect): Boolean {
        val isRemoved = _aspectPool.remove(aspect)

        if (isRemoved) {
            changedAspectPool.deleteIfDependentOnAspect(aspect)
            if (aspect !is ConverseWrapper)
                _potentialConverseWrappers.removeIf { it.aspect == aspect }
        }

        return isRemoved
    }

    override fun toString() = """
        |Aspects:
        |${aspectPool.all.joinToString("\n\n")}
    """.trimMargin()
}


data class AspectOption(
    val aspect: ConverseWrapper,
    val dependencies: AspectDependencies,
    val group: Group?,
    val needs: MutableCollection<Pair<ResourceNeed, ResourceTag>>
)


typealias SourcedAspect = Pair<ConverseWrapper, Group?>
