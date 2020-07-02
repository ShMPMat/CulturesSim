package simulation.culture.group.request

import simulation.culture.aspect.hasMeaning
import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.space.resource.Resource

fun resourceToRequest(resource: Resource, group: Group, amount: Int): Request {
    val amountD = amount.toDouble()
    if (resource.hasMeaning) {
        //TODO read native!!!!!
        return MeaningResourceRequest(
                group,
                group.cultureCenter.meaning,
                resource,
                amountD,
                amountD,
                passingReward,
                passingReward

        )
    } else {
        return ResourceRequest(
                group,
                resource,
                amountD,
                amountD,
                passingReward,
                passingReward
        )
    }
}