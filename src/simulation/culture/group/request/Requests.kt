package simulation.culture.group.request

import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.space.resource.Resource

fun resourceToRequest(resource: Resource, group: Group, amount: Int): Request {
    if (resource.hasMeaning()) {
        //TODO read native!!!!!
        return MeaningResourceRequest(
                group,
                group.cultureCenter.meaning,
                resource,
                amount,
                amount,
                passingReward,
                passingReward

        )
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