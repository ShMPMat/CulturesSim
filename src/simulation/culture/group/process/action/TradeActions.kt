package simulation.culture.group.process.action

import simulation.culture.group.centers.Group
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.container.ResourcePromisePack
import kotlin.math.log


class TradeEvaluateResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run(): Int {
        val traderSkill = group.populationCenter.stratumCenter.traderStratum.effectiveness

        return (EvaluateResourcesA(group, pack).run() / log(traderSkill + 1.0, 2.0)).toInt()
    }

    override val internalToString  = "Let ${group.name} evaluate the trade value of ${pack.listResources}"
}

class RequestStockA(group: Group): AbstractGroupAction(group) {
    override fun run(): ResourcePromisePack {

        return group.populationCenter.stratumCenter.traderStratum.stock
    }

    override val internalToString = "Get the trade stock of ${group.name}"
}
