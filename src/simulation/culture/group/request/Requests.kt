package simulation.culture.group.request

import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.space.resource.Resource

fun resourceToRequest(resource: Resource, group: Group, amount: Int): Request {
    if (resource.hasMeaning()) {
        val k = 9
        TODO()
    } else {
        return ResourceRequest(
                group,
                resource,
                amount,
                amount,
                passingReward,
                passingReward
        )
    }
}