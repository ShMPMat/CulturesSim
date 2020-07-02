package simulation.culture.group.request

import simulation.culture.group.centers.Group
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource

class ResourceRequest(
        group: Group,
        private val resource: Resource,
        floor: Double,
        ceiling: Double,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) : Request(group, floor, ceiling, penalty, reward) {
    override fun reducedAmountCopy(amount: Double) = ResourceRequest(
            group,
            resource,
            0.0,
            amount,
            penalty,
            reward
    )

    override val evaluator = resourceEvaluator(resource)

    override fun reassign(group: Group) = ResourceRequest(group, resource, floor, ceiling, penalty, reward)

    override fun toString() = "Resource ${resource.baseName}"
}