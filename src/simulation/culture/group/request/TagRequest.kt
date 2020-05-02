package simulation.culture.group.request

import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.culture.group.centers.Group
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag

class TagRequest(
        group: Group,
        private val tag: ResourceTag,
        floor: Int,
        ceiling: Int,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) : Request(group, floor, ceiling, penalty, reward) {
    private fun hasTagFrom(tags: Collection<ResourceTag>) = tags.contains(tag)

    override fun isAcceptable(stratum: Stratum): ResourceEvaluator? {
        if (stratum !is AspectStratum)
            return null
        return if (hasTagFrom(stratum.aspect.tags))
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