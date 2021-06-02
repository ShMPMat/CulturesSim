package shmp.simulation.culture.group.request

import shmp.simulation.culture.aspect.AspectController
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.stratum.AspectStratum
import shmp.simulation.culture.group.stratum.Stratum
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.container.ResourcePack
import shmp.utils.addLinePrefix
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

    open fun isAcceptable(stratum: Stratum) = when {
        stratum !is AspectStratum -> null
        evaluator.evaluate(stratum.aspect.producedResources) > 0 -> evaluator
        else -> null
    }

    fun amountLeft(resourcePack: ResourcePack) = max(0.0, core.ceiling - evaluator.evaluate(resourcePack))

    fun isFloorSatisfied(resourcePack: ResourcePack) = evaluator.evaluate(resourcePack) >= core.floor

    fun end(resourcePack: MutableResourcePack): Result {
        val partPack = evaluator.pick(
                core.ceiling,
                resourcePack.resources,
                { listOf(it.copy(1)) },
                { r, n -> listOf(r.getCleanPart(n, group.populationCenter.taker)) }
        )
        val amount = evaluator.evaluate(partPack)
        val neededCopy = ResourcePack(evaluator.pick(partPack).resources.map { it.exactCopy() })

        if (amount < core.floor) {
            core.penalty(Pair(core.group, partPack), amount / core.floor)
            return Result(ResultStatus.NotSatisfied, neededCopy)
        } else
            core.reward(Pair(core.group, partPack), amount / core.floor - 1)
        return if (amount < core.ceiling) Result(ResultStatus.Satisfied, neededCopy)
        else Result(ResultStatus.Excellent, neededCopy)
    }

    fun satisfactionLevel(sample: Resource) = evaluator.evaluate(sample.copy(1))

    fun satisfactionLevel(stratum: Stratum) =
            if (stratum !is AspectStratum) 0.0
            else stratum.aspect.producedResources
                    .map { satisfactionLevel(it) }
                    .foldRight(0.0, Double::plus)

    open fun getController(ignoreAmount: Int) = AspectController(
            1,
            core.ceiling - ignoreAmount,
            core.floor - ignoreAmount,
            evaluator,
            core.group.populationCenter,
            core.group.territoryCenter.accessibleTerritory,
            false,
            core.group
    )

    open fun finalFilter(pack: MutableResourcePack) = evaluator.pickAndRemove(pack)

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
