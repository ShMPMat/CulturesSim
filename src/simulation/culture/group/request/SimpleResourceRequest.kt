package simulation.culture.group.request

import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.culture.group.centers.Group
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource

class SimpleResourceRequest(
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
        for (aspect in stratum.aspects)
            if (aspect is ConverseWrapper)
                if (aspect.producedResources.any { it.simpleName == resource.simpleName })
                    return evaluator
        return null
    }

    override fun reducedAmountCopy(amount: Int) = SimpleResourceRequest(
            group,
            resource,
            0,
            amount,
            penalty,
            reward
    )

    override val evaluator = simpleResourceEvaluator(resource)

    override fun satisfactionLevel(sample: Resource): Int {
        return if (resource.simpleName === sample.simpleName) 1 else 0
    }

    override fun toString(): String {
        return "Resource with simple name ${resource.simpleName}"
    }

}