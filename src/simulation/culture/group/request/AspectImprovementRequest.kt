package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.culture.group.centers.Group
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.tag.AspectImprovementTag
import simulation.space.resource.tag.ResourceTag

class AspectImprovementRequest(
        group: Group,
        private val aspect: Aspect,
        floor: Int,
        ceiling: Int,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) : Request(group, floor, ceiling, penalty, reward) {

    override fun isAcceptable(stratum: Stratum): ResourceEvaluator? {
        if (stratum !is AspectStratum)
            return null
        return if (stratum.aspects.any { cw ->
                    cw.resource.tags.filterIsInstance<AspectImprovementTag>().any { it.labeler.isSuitable(aspect) }
                })
            evaluator
        else null
    }

    override fun reducedAmountCopy(amount: Int) = AspectImprovementRequest(
            group,
            aspect,
            0,
            amount,
            penalty,
            reward
    )

    override val evaluator = aspectEvaluator(aspect)

    override fun satisfactionLevel(sample: Resource): Int {
        return sample.tags.filterIsInstance<AspectImprovementTag>().count { it.labeler.isSuitable(aspect) }
    }

    override fun toString(): String {
        return "want improvement for ${aspect.name}"
    }

}