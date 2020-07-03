package simulation.culture.group.request

import simulation.culture.group.centers.Group
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource

class SimpleResourceRequest(
        group: Group,
        private val resource: Resource,
        floor: Double,
        ceiling: Double,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        need: Int
) : Request(group, floor, ceiling, penalty, reward, need) {
    override fun reducedAmountCopy(amount: Double) = SimpleResourceRequest(
            group,
            resource,
            0.0,
            amount,
            penalty,
            reward,
            need
    )

    override val evaluator = simpleResourceEvaluator(resource)

    override fun reassign(group: Group) = SimpleResourceRequest(
            group,
            resource,
            floor,
            ceiling,
            penalty,
            reward,
            need
    )

    override fun toString() = "Resource with simple name ${resource.simpleName}"
}
