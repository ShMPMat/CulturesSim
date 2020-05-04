package simulation.culture.group.request

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
    override fun reducedAmountCopy(amount: Int) = SimpleResourceRequest(
            group,
            resource,
            0,
            amount,
            penalty,
            reward
    )

    override val evaluator = simpleResourceEvaluator(resource)

    override fun reassign(group: Group) = SimpleResourceRequest(group, resource, floor, ceiling, penalty, reward)

    override fun toString(): String {
        return "Resource with simple name ${resource.simpleName}"
    }

}