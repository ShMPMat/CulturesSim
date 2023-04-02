package io.tashtabash.simulation.culture.group.process.interaction

import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.group.process.action.*
import io.tashtabash.simulation.culture.group.process.emptyProcessResult
import io.tashtabash.simulation.culture.thinking.meaning.makeResourcePackMemes
import io.tashtabash.simulation.culture.thinking.meaning.makeStratumMemes
import io.tashtabash.simulation.event.Type
import io.tashtabash.simulation.space.resource.container.ResourcePack


class TradeI(
        initiator: Group,
        participator: Group,
        val amount: Int
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        val wantedResources = ChooseResourcesA(
                initiator,
                RequestStockA(participator).run(),
                amount
        ).run()

        val priceForP = TradeEvaluateResourcesA(participator, wantedResources.makeCopy()).run()
        if (priceForP == 0)
            return emptyProcessResult to emptyProcessResult

        val priceInResources = ChooseResourcesA(
                participator,
                RequestStockA(initiator).run(),
                priceForP,
                wantedResources.makeCopy().resources
        ).run()
        val priceForI = TradeEvaluateResourcesA(participator, priceInResources.makeCopy()).run()

        if (priceForP > priceForI)
            return emptyProcessResult to emptyProcessResult

        val got = wantedResources.extract(initiator.populationCenter.taker)
        val given = priceInResources.extract(participator.populationCenter.taker)
        var result = ProcessResult(Event(
                Type.Cooperation,
                "${initiator.name} and ${participator.name} " +
                        "traded ${got.listResources} - $priceForP for ${given.listResources} - $priceForI"
        ))

        result += SwapResourcesI(initiator, participator, got, given).run()

        ChangeRelationsI(initiator, participator, 0.5).run()
        IncStratumImportanceA(
                initiator,
                initiator.populationCenter.stratumCenter.traderStratum,
                1
        ).run()

        return result + ProcessResult(makeStratumMemes(initiator.populationCenter.stratumCenter.traderStratum)) to
                ProcessResult(makeStratumMemes(participator.populationCenter.stratumCenter.traderStratum))
    }
}


class SwapResourcesI(
        initiator: Group,
        participator: Group,
        private val gotPack: ResourcePack,
        private val givePack: ResourcePack
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
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
        )) +
                ProcessResult(makeResourcePackMemes(gotPack) + makeResourcePackMemes(givePack)) to
                ProcessResult(makeResourcePackMemes(gotPack) + makeResourcePackMemes(givePack))
    }
}
