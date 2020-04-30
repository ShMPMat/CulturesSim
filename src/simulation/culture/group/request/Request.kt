package simulation.culture.group.request

import simulation.culture.aspect.ConverseWrapper
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
    abstract fun isAcceptable(stratum: Stratum): ResourceEvaluator?

    abstract fun reducedAmountCopy(amount: Int): Request

    abstract val evaluator: ResourceEvaluator

    abstract fun satisfactionLevel(sample: Resource): Int

    fun amountLeft(resourcePack: ResourcePack) = max(0, ceiling - evaluator.evaluate(resourcePack))

    fun end(resourcePack: MutableResourcePack) : Result {
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

    fun satisfactionLevel(stratum: Stratum): Int {
        if (stratum !is AspectStratum) return 0
        var result = 0
        for (converseWrapper in stratum.aspects.filterIsInstance<ConverseWrapper>()) {
            result += converseWrapper.producedResources
                    .map { this.satisfactionLevel(it) }
                    .foldRight(0, Int::plus)
        }
        return result
    }
}

data class Result(val status: ResultStatus, val pack: ResourcePack) {
    override fun toString() = "$status, \n" +
            pack.toString().lines().map { "  $it" }.joinToString("\n")
}

enum class ResultStatus {
    NotSatisfied,
    Satisfied,
    Excellent
}