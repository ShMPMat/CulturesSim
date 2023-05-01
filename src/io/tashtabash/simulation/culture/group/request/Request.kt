package io.tashtabash.simulation.culture.group.request

import io.tashtabash.simulation.culture.aspect.AspectController
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.stratum.AspectStratum
import io.tashtabash.simulation.culture.group.stratum.Stratum
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.resource.container.ResourcePack
import io.tashtabash.utils.addLinePrefix
import kotlin.math.max


//Represents a Resource request which may by executed by a Group.
abstract class Request(val core: RequestCore) {
    val group
        get() = core.group
    val floor
        get() = core.floor
    val ceiling
        get() = core.ceiling
    val penalty
        get() = core.penalty
    val reward
        get() = core.reward
    val need
        get() = core.need
    val types: Set<RequestType>
        get() = core.requestTypes

    abstract fun reducedAmountCopy(amount: Double): Request

    abstract val evaluator: ResourceEvaluator

    abstract fun reassign(group: Group): Request

    open fun isAcceptable(stratum: AspectStratum): Boolean {
        val pair = stratum.aspect.name to toString()
        stratumRequestHash[pair]?.let {
            return it
        }

        val isAcceptable = evaluator.hasValue(stratum.aspect.producedResources)

        stratumRequestHash[pair] = isAcceptable

        return isAcceptable
    }

    fun amountLeft(resourcePack: ResourcePack) = max(0.0, core.ceiling - evaluator.evaluatePack(resourcePack))

    fun isFloorSatisfied(resources: List<Resource>) = evaluator.evaluate(resources) >= core.floor

    fun end(resourcePack: MutableResourcePack): Result {
        val partPack = evaluator.pick(
                core.ceiling,
                resourcePack.resources,
                { it.core.wrappedSample },
                { r, n -> listOf(r.getCleanPart(n, group.populationCenter.taker)) }
        )
        val amount = evaluator.evaluate(partPack)
        val neededCopy = ResourcePack(evaluator.pick(partPack).map { it.exactCopy() })

        if (amount < core.floor) {
            core.penalty(core.group to MutableResourcePack(partPack), amount / core.floor)
            return Result(ResultStatus.NotSatisfied, neededCopy)
        } else
            core.reward(core.group to MutableResourcePack(partPack), amount / core.floor - 1)
        return if (amount < core.ceiling)
            Result(ResultStatus.Satisfied, neededCopy)
        else Result(ResultStatus.Excellent, neededCopy)
    }

    fun satisfactionLevel(sample: Resource) = evaluator.evaluate(sample.core.sample)

    fun satisfactionLevel(stratum: Stratum) =
            if (stratum !is AspectStratum) 0.0
            else stratum.aspect.producedResources.sumOf { satisfactionLevel(it) }

    open fun getController(ignoreAmount: Int) = AspectController(
            1,
            core.ceiling - ignoreAmount,
            core.floor - ignoreAmount,
            evaluator,
            core.group.populationCenter,
            core.group.territoryCenter.accessibleTerritory,
            false,
            core.group,
            types
    )

    open fun finalFilter(pack: MutableResourcePack) = evaluator.pickAndRemove(pack)

    open fun finalFilter(pack: MutableList<Resource>) = evaluator.pickAndRemove(pack)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Request

        if (toString() != other.toString()) return false

        return true
    }

    override fun hashCode() = toString().hashCode()
}

data class Result(val status: ResultStatus, val pack: ResourcePack) {
    override fun toString() = """
        |$status, 
        |${pack.addLinePrefix("  ") }
        """.trimMargin()
}

enum class ResultStatus {
    NotSatisfied,
    Satisfied,
    Excellent
}

private val stratumRequestHash = mutableMapOf<Pair<String, String>, Boolean>()
