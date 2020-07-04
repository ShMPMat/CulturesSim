package simulation.culture.group.request

import simulation.culture.aspect.hasMeaning
import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.space.resource.Resource

fun resourceToRequest(resource: Resource, group: Group, amount: Int, need: Int): Request {
    val amountD = amount.toDouble()
    if (resource.hasMeaning) {
        //TODO read native!!!!!
        return MeaningResourceRequest(
                group.cultureCenter.meaning,
                resource,
                RequestCore(group, amountD, amountD, passingReward, passingReward, need)
        )
    } else {
        return ResourceRequest(
                resource,
                RequestCore(group, amountD, amountD, passingReward, passingReward, need)
        )
    }
}