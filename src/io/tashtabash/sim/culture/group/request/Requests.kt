package io.tashtabash.sim.culture.group.request

import io.tashtabash.sim.culture.aspect.getMeaning
import io.tashtabash.sim.culture.aspect.hasMeaning
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.passingReward
import io.tashtabash.sim.space.resource.Resource


fun resourceToRequest(
        resource: Resource,
        group: Group,
        amount: Int = 1,
        need: Int = 1,
        requestTypes: Set<RequestType> = setOf()
): Request {
    val amountD = amount.toDouble()
    if (resource.hasMeaning) {
        //TODO read native!!!!!
        return MeaningResourceRequest(
                resource.getMeaning?.meme ?: group.cultureCenter.meaning,
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
