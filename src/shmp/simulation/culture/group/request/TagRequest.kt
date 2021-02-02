package shmp.simulation.culture.group.request

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.resource.tag.ResourceTag

class TagRequest(private val tag: ResourceTag, core: RequestCore) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            TagRequest(tag, core.copy(floor = 0.0, ceiling = amount))

    override val evaluator = tagEvaluator(tag)

    override fun reassign(group: Group) = TagRequest(tag, core.copy(group = group))

    override fun toString() = "want for $tag"
}
