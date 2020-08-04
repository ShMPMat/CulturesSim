package simulation.culture.group.process.action

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.container.ResourcePromisePack
import kotlin.math.log


class TradeEvaluateResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run(): Int {
        val traderSkill = group.populationCenter.stratumCenter.traderStratum.effectiveness

        return (EvaluateResourcesA(group, pack).run() / log(traderSkill + 1.0, 2.0)).toInt()
    }

    override val internalToString = "Let ${group.name} evaluate the trade value of ${pack.listResources}"
}


class MakeTradeResourcesA(group: Group, val amount: Int) : AbstractGroupAction(group) {
    override fun run(): ResourcePack {
        val resources = group.cultureCenter.aspectCenter.aspectPool.producedResources

        if (resources.isEmpty())
            return ResourcePack()

        val chosenResource = randomElement(resources, Controller.session.random)//TODO maybe mapper?
        return ProduceExactResourceA(group, chosenResource, amount, 20).run()
    }

    override val internalToString = "Let ${group.name} to try make Resources for Trade"
}


class RequestStockA(group: Group) : AbstractGroupAction(group) {
    override fun run(): ResourcePromisePack {
        val traderStratum = group.populationCenter.stratumCenter.traderStratum

        if (traderStratum.stock.resources.isEmpty()) {
            val resources = MakeTradeResourcesA(group, 100).run()
            val isEmpty = resources.isEmpty

            group.populationCenter.turnResources.addAll(resources)

            if (!isEmpty)
                traderStratum.tradeStockUpdate(group)
        }

        return traderStratum.stock
    }

    override val internalToString = "Get the trade stock of ${group.name}"
}
