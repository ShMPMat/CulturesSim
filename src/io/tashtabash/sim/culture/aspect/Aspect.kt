package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.CultureParameters
import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.culture.group.centers.AspectCenter
import io.tashtabash.sim.culture.group.request.ResourceEvaluator
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import java.util.*
import kotlin.math.ceil
import kotlin.math.max


open class Aspect(var core: AspectCore, dependencies: AspectDependencies) {
    /**
     * Map which stores for every requirement some Dependencies, from which
     * Aspect can get resource for use
     */
    var dependencies = AspectDependencies(mutableMapOf())

    init {
        initDependencies(dependencies)
    }

    private var usedThisTurn = false
    var usefulness = CultureParameters.defaultAspectUsefulness
        private set

    var isCurrentlyUsed = false

    var canInsertMeaning = false

    fun initDependencies(dependencies: AspectDependencies) {
        for ((key, value) in dependencies.map)
            this.dependencies.map[key] = value.map { it.copy() }.toMutableSet()
    }

    open fun swapDependencies(aspectCenter: AspectCenter) =
            dependencies.map.values.forEach { set -> set.forEach { it.swapDependencies(aspectCenter) } }

    val name = core.name

    val tags = core.tags

    val requirements = core.requirements

    open val isValid = true

    open val producedResources: List<Resource> = emptyList()

    val canApplyMeaning = core.applyMeaning

    fun canReturnMeaning() = this is ConverseWrapper && this.canInsertMeaning

    open fun checkDependencies(dependencies: AspectDependencies) = requirements.size == dependencies.size

    open fun canTakeResources() = false

    fun addOneDependency(newDependencies: AspectDependencies) {
        for (tag in dependencies.map.keys) try {//TODO why one, add a l l
            for (dependency1 in newDependencies.map.getValue(tag)) {
                if (!dependencies.map.getValue(tag).contains(dependency1)) {
                    dependencies.map.getValue(tag).add(dependency1)
                    break
                }
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }

    open fun copy(dependencies: AspectDependencies) = core.makeAspect(dependencies)

    fun calculateNeededWorkers(evaluator: ResourceEvaluator, amount: Double) = ceil(max(
            evaluator.getSatisfiableAmount(amount, producedResources) * core.standardComplexity,
            1.0
    )).toInt()

    fun calculateProducedValue(evaluator: ResourceEvaluator, workers: Int) =
            (evaluator.evaluate(producedResources) * workers) / core.standardComplexity

    protected open fun shouldPassMeaningNeed(isMeaningNeeded: Boolean) = isMeaningNeeded

    fun markAsUsed() = gainUsefulness(1)

    fun gainUsefulness(amount: Int) {
        if (amount <= 0) return
        usefulness = max(usefulness + amount, CultureParameters.defaultAspectUsefulness)
        usedThisTurn = true
    }

    open fun finishUpdate() {
        if (!usedThisTurn)
            usefulness--
        usedThisTurn = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other == null || javaClass != other.javaClass)
            return false

        val aspect = other as Aspect
        return core.name == aspect.core.name
    }

    override fun hashCode() = Objects.hash(core.name)

    override fun toString() = "Aspect ${core.name} usefulness - $usefulness dependencies:\n" +
            dependencies.map.entries.joinToString("\n") { (tag, deps) ->
                "**${tag.name}:\n" +
                        deps.joinToString("\n") { "****${it.name}" }
            }
}

typealias Need = Pair<ResourceLabeler, Int>
