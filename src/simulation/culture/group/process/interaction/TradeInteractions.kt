package simulation.culture.group.process.interaction

import simulation.event.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.*
import simulation.space.resource.container.ResourcePack


class TradeI(
        initiator: Group,
        participator: Group,
        val amount: Int
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val wantedResources = ChooseResourcesA(
                initiator,
                participator.populationCenter.stratumCenter.traderStratum.stock,
                amount
        ).run()
        val priceForP = TradeEvaluateResourcesA(participator, wantedResources.makeCopy()).run()
        if (priceForP == 0)
            return emptyList()

        val priceInResources = ChooseResourcesA(
                participator,
                participator.populationCenter.stratumCenter.traderStratum.stock,
                priceForP,
                wantedResources.makeCopy().resources
        ).run()
        val priceForI = TradeEvaluateResourcesA(participator, priceInResources.makeCopy()).run()

        if (priceForP > priceForI)
            return emptyList()

        val got = wantedResources.extract()
        val given = priceInResources.extract()
        val events = mutableListOf(Event(
                Event.Type.Cooperation,
                "${initiator.name} and ${participator.name} " +
                        "traded $got - $priceForP for $given - $priceForI".replace("\n", " ")
        ))

        events.addAll(SwapResourcesI(initiator, participator, got, given).run())

        ChangeRelationsI(initiator, participator, 0.5).run()
        IncStratumImportanceA(
                initiator,
                initiator.populationCenter.stratumCenter.traderStratum,
                1
        ).run()

        return events
    }
}


class SwapResourcesI(
        initiator: Group,
        participator: Group,
        private val gotPack: ResourcePack,
        private val givePack: ResourcePack
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
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

        return listOf(Event(
                Event.Type.GroupInteraction,
                "${initiator.name} and ${participator.name} begun swapping of $gotPack and $givePack"
        ))
    }
}
