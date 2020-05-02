package simulation.culture.group.request

import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.culture.group.centers.Group
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource

class ResourceRequest(
        group: Group,
        private val resource: Resource,
        floor: Int,
        ceiling: Int,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) : Request(group, floor, ceiling, penalty, reward) {
    override fun isAcceptable(stratum: Stratum): ResourceEvaluator? {
        if (stratum !is AspectStratum)
            return null
        if (stratum.aspect is ConverseWrapper)
            if (stratum.aspect.producedResources.any { it.baseName == resource.baseName })
                return evaluator
        return null
    }

    override fun reducedAmountCopy(amount: Int) = ResourceRequest(
            group,
            resource,
            0,
            amount,
            penalty,
            reward
    )

    override val evaluator = resourceEvaluator(resource)

    override fun satisfactionLevel(sample: Resource): Int {
        return if (resource.baseName === sample.baseName) 1 else 0
    }

    override fun toString(): String {
        return "Resource ${resource.baseName}"
    }

}