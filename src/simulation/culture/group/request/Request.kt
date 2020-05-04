package simulation.culture.group.request

import simulation.culture.aspect.AspectController
import simulation.culture.group.centers.Group
import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import kotlin.math.max

/**
 * Class which represents a request which may by executed by a Group.
 */
abstract class Request(
        val group: Group,
        var floor: Int,
        var ceiling: Int,
        var penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        var reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) {
    abstract fun reducedAmountCopy(amount: Int): Request

    abstract val evaluator: ResourceEvaluator

    abstract fun reassign(group: Group): Request

    fun isAcceptable(stratum: Stratum) = when {
        stratum !is AspectStratum -> null
        evaluator.evaluate(stratum.aspect.producedResources) > 0 -> evaluator
        else -> null
    }

    fun amountLeft(resourcePack: ResourcePack) = max(0, ceiling - evaluator.evaluate(resourcePack))

    fun end(resourcePack: MutableResourcePack): Result {
        val partPack = evaluator.pick(
                ceiling,
                resourcePack.resources,
                { listOf(it.copy(1)) },
                { r, n -> listOf(r.getCleanPart(n)) }
        )
        val amount = evaluator.evaluate(partPack)
        val neededCopy = ResourcePack(evaluator.pick(partPack).resources.map { it.copy(it.amount) })

        if (amount < floor) {
            penalty(Pair(group, partPack), amount / floor.toDouble())
            return Result(ResultStatus.NotSatisfied, neededCopy)
        } else
            reward(Pair(group, partPack), amount / floor.toDouble() - 1)
        return if (amount < ceiling) Result(ResultStatus.Satisfied, neededCopy)
        else Result(ResultStatus.Excellent, neededCopy)
    }

    fun satisfactionLevel(sample: Resource) = evaluator.evaluate(sample.copy(1))

    fun satisfactionLevel(stratum: Stratum) =
            if (stratum !is AspectStratum) 0
            else stratum.aspect.producedResources
                    .map { satisfactionLevel(it) }
                    .foldRight(0, Int::plus)

    open fun getController(ignoreAmount: Int) = AspectController(
            1,
            ceiling - ignoreAmount,
            floor - ignoreAmount,
            evaluator,
            group.populationCenter,
            group.territoryCenter.accessibleTerritory,
            false,
            group,
            group.cultureCenter.meaning
    )
}

data class Result(val status: ResultStatus, val pack: ResourcePack) {
    override fun toString() = "$status, \n" +
            pack.toString().lines().joinToString("\n") { "  $it" }
}

enum class ResultStatus {
    NotSatisfied,
    Satisfied,
    Excellent
}