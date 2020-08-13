package simulation.culture.group.process.interaction

import simulation.event.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.*
import simulation.culture.group.process.emptyProcessResult
import simulation.event.Type
import simulation.space.resource.container.ResourcePack


class TradeI(
        initiator: Group,
        participator: Group,
        val amount: Int
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): ProcessResult {
        val wantedResources = ChooseResourcesA(
                initiator,
                RequestStockA(participator).run(),
                amount
        ).run()
        val priceForP = TradeEvaluateResourcesA(participator, wantedResources.makeCopy()).run()
        if (priceForP == 0)
            return emptyProcessResult

        val priceInResources = ChooseResourcesA(
                participator,
                RequestStockA(initiator).run(),
                priceForP,
                wantedResources.makeCopy().resources
        ).run()
        val priceForI = TradeEvaluateResourcesA(participator, priceInResources.makeCopy()).run()

        if (priceForP > priceForI)
            return emptyProcessResult

        val got = wantedResources.extract()
        val given = priceInResources.extract()
        var result = ProcessResult(Event(
                Type.Cooperation,
                "${initiator.name} and ${participator.name} " +
                        "traded $got - $priceForP for $given - $priceForI".replace("\n", " ")
        ))

        result += SwapResourcesI(initiator, participator, got, given).run()

        ChangeRelationsI(initiator, participator, 0.5).run()
        IncStratumImportanceA(
                initiator,
                initiator.populationCenter.stratumCenter.traderStratum,
                1
        ).run()

        return result
    }
}


class SwapResourcesI(
        initiator: Group,
        participator: Group,
        private val gotPack: ResourcePack,
        private val givePack: ResourcePack
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): ProcessResult {
        ScheduleActionA(
                participator,
                ReceivePopulationResourcesA(initiator, gotPack),
                ComputeTravelTime(participator, initiator).run()
        ).run()
        ScheduleActionA(
                initiator,
                ReceivePopulationResourcesA(participator, givePack),
                ComputeTravelTime(initiator, participator).run()
        ).run()

        return ProcessResult(Event(
                Type.GroupInteraction,
                "${initiator.name} and ${participator.name} begun swapping of $gotPack and $givePack"
        ))
    }
}
