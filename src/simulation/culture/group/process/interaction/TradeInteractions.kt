package simulation.culture.group.process.interaction

import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.ChooseResourcesA
import simulation.culture.group.process.action.IncStratumImportanceA
import simulation.culture.group.process.action.ReceivePopulationResourcesA
import simulation.culture.group.process.action.TradeEvaluateResourcesA
import simulation.culture.group.stratum.TraderStratum


class TradeInteraction(
        initiator: Group,
        participator: Group,
        val amount: Int
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val wantedResources = ChooseResourcesA(
                initiator,
                participator.populationCenter.turnResources,
                amount
        ).run()
        val priceForP = TradeEvaluateResourcesA(participator, wantedResources.makeCopy()).run()
        if (priceForP == 0) {
            return emptyList()
        }

        val priceInResources = ChooseResourcesA(
                participator,
                initiator.populationCenter.turnResources,
                priceForP,
                wantedResources.makeCopy().resources
        ).run()
        val priceForI = TradeEvaluateResourcesA(participator, priceInResources.makeCopy()).run()

        if (priceForP <= priceForI) {
            val got = wantedResources.extract()
            val given = priceInResources.extract()
            val event = Event(
                    Event.Type.GroupInteraction,
                    "Groups ${initiator.name} and ${participator.name} " +
                            "traded $got - $priceForP and $given - $priceForI".replace("\n", " ")
            )

            ReceivePopulationResourcesA(initiator, got).run()
            ReceivePopulationResourcesA(participator, given).run()

            RelationsChangeInteraction(initiator, participator, 0.001).run()
            IncStratumImportanceA(
                    initiator,
                    initiator.populationCenter.stratumCenter.traderStratum,
                    1
            ).run()

            return listOf(event)
        }
        return emptyList()
    }
}
