package simulation.culture.group.request

import simulation.culture.group.centers.Group
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.tag.ResourceTag

class TagRequest(
        group: Group,
        private val tag: ResourceTag,
        floor: Int,
        ceiling: Int,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) : Request(group, floor, ceiling, penalty, reward) {
    override fun reducedAmountCopy(amount: Int) = TagRequest(group, tag, 0, amount, penalty, reward)

    override val evaluator = tagEvaluator(tag)

    override fun reassign(group: Group) = TagRequest(group, tag, floor, ceiling, penalty, reward)

    override fun toString() = "want for $tag"
}