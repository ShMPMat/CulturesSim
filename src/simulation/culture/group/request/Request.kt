package simulation.culture.group.request

import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.Group
import simulation.culture.group.Stratum
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import java.util.function.BiFunction

/**
 * Class which represents a request which may by executed by a Group.
 */
abstract class Request(
        protected var group: Group,
        var floor: Int,
        var ceiling: Int,
        var penalty: BiFunction<Pair<Group, MutableResourcePack>, Double, Void>,
        var reward: BiFunction<Pair<Group, MutableResourcePack>, Double, Void>
) {
    abstract fun isAcceptable(stratum: Stratum): ResourceEvaluator?

    abstract val evaluator: ResourceEvaluator

    abstract fun satisfactionLevel(sample: Resource): Int

    fun end(resourcePack: MutableResourcePack) {
        val partPack = evaluator.pick(
                ceiling,
                resourcePack.resources,
                { listOf(it.copy(1)) },
                { r, n -> listOf(r.getCleanPart(n)) }
        )
        val amount = evaluator.evaluate(partPack)

        if (amount < floor)
            penalty.apply(Pair(group, partPack), amount / floor.toDouble())
        else
            reward.apply(Pair(group, partPack), amount / floor.toDouble() - 1)
    }

    fun satisfactionLevel(stratum: Stratum): Int {
        var result = 0
        for (converseWrapper in stratum.aspects.filterIsInstance<ConverseWrapper>()) {
            result += converseWrapper.producedResources
                    .map { this.satisfactionLevel(it) }
                    .foldRight(0, Int::plus)
        }
        return result
    }

}