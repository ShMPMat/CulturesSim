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

    abstract fun end(resourcePack: MutableResourcePack)

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