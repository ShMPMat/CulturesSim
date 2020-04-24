package simulation.culture.group.request

import simulation.culture.group.AspectStratum
import simulation.culture.group.Stratum
import simulation.culture.group.centers.Group
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import java.util.function.BiFunction
import kotlin.math.max

class TagRequest(
        group: Group,
        private val tag: ResourceTag,
        floor: Int,
        ceiling: Int,
        penalty: BiFunction<Pair<Group, MutableResourcePack>, Double, Void>,
        reward: BiFunction<Pair<Group, MutableResourcePack>, Double, Void>
) : Request(group, floor, ceiling, penalty, reward) {
    private fun hasTagFrom(tags: Collection<ResourceTag>) = tags.contains(tag)

    override fun isAcceptable(stratum: Stratum): ResourceEvaluator? {
        if (stratum !is AspectStratum)
            return null
        return if (stratum.aspects.any { hasTagFrom(it.tags) })
            evaluator
        else null
    }

    override fun reducedAmountCopy(amount: Int) = TagRequest(
            group,
            tag,
            0,
            amount,
            penalty,
            reward
    )

    override val evaluator = tagEvaluator(tag)

    override fun satisfactionLevel(sample: Resource): Int {
        val index = sample.tags.indexOf(tag)
        return if (index == -1) 0 else sample.tags[index].level
    }

    override fun toString(): String {
        return "want for $tag"
    }

}