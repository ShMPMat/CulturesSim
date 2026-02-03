package io.tashtabash.sim.culture.group.process.interaction

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.action.*
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.culture.thinking.meaning.makeResourcePackMemes
import io.tashtabash.sim.culture.thinking.meaning.makeStratumMemes
import io.tashtabash.sim.event.Cooperation
import io.tashtabash.sim.event.IntergroupInteraction
import io.tashtabash.sim.event.of
import io.tashtabash.sim.space.resource.container.ResourcePack


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
        var result = ProcessResult(
                Cooperation of
                        "${initiator.name} and ${participator.name} " +
                        "traded ${got.listResources} - $priceForP for ${given.listResources} - $priceForI"
        )

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

        return ProcessResult(
                IntergroupInteraction of
                        "${initiator.name} and ${participator.name} began swapping $gotPack for $givePack"
        ) +
                ProcessResult(makeResourcePackMemes(gotPack) + makeResourcePackMemes(givePack)) to
                ProcessResult(makeResourcePackMemes(gotPack) + makeResourcePackMemes(givePack))
    }
}
