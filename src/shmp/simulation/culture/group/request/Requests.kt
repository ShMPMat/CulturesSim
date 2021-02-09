package shmp.simulation.culture.group.request

import shmp.simulation.culture.aspect.hasMeaning
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.passingReward
import shmp.simulation.space.resource.Resource


fun resourceToRequest(
        resource: Resource,
        group: Group,
        amount: Int,
        need: Int,
        requestTypes: Set<RequestType>
): Request {
    val amountD = amount.toDouble()
    if (resource.hasMeaning) {
        //TODO read native!!!!!
        return MeaningResourceRequest(
                group.cultureCenter.meaning,
                resource,
                RequestCore(group, amountD, amountD, passingReward, passingReward, need, requestTypes)
        )
    } else {
        return ResourceRequest(
                resource,
                RequestCore(group, amountD, amountD, passingReward, passingReward, need, requestTypes)
        )
    }
}
