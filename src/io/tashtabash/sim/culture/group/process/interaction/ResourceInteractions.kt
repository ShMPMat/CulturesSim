package io.tashtabash.sim.culture.group.process.interaction

import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.culture.group.HelpEvent
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.toPositiveChange
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.action.*
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.culture.group.request.Request
import io.tashtabash.sim.culture.thinking.meaning.makeResourcePackMemes
import io.tashtabash.sim.event.Conflict
import io.tashtabash.sim.event.Cooperation
import io.tashtabash.sim.event.of
import io.tashtabash.sim.space.resource.container.ResourcePromise
import io.tashtabash.sim.space.resource.container.ResourcePromisePack
import kotlin.math.pow


class RequestHelpI(
        initiator: Group,
        participator: Group,
        val request: Request
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        if (!CooperateA(participator, initiator, 0.5).run())
            return emptyProcessResult to emptyProcessResult

        val given = ExecuteRequestA(participator, request.reassign(participator)).run()
        val delivery = ScheduleActionA(
                participator,
                ReceiveRequestResourcesA(initiator, request, given),
                ComputeTravelTime(participator, initiator).run()
        )

        return if (given.isNotEmpty) {
            delivery.run()
            ChangeRelationsA(initiator, participator, request.evaluator.evaluatePack(given) / request.ceiling).run()

            ProcessResult(HelpEvent(
                    "${initiator.name} got help in $request from ${participator.name}: " +
                            "${given.listResources}, with delay ${delivery.delay}",
                    request.evaluator.evaluatePack(given)
            )) +
                    ProcessResult(makeResourcePackMemes(given)) +
                    ProcessResult(Trait.Peace.toPositiveChange() * 3.0) to
                    ProcessResult(makeResourcePackMemes(given)) +
                    ProcessResult(Trait.Peace.toPositiveChange() * 5.0)
        } else emptyProcessResult to emptyProcessResult
    }
}


class GiveGiftI(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        val relation = initiator.relationCenter.getNormalizedRelation(participator)

        val gift = ChooseResourcesA(
                initiator,
                ResourcePromisePack(initiator.populationCenter.turnResources.resources.map { ResourcePromise(it) }),
                (1000 * (1 - relation) + 1).toInt()
        ).run()

        return ReceiveGiftI(initiator, participator, gift).run() to emptyProcessResult
    }
}

class ReceiveGiftI(
        initiator: Group,
        participator: Group,
        val gift: ResourcePromisePack
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        val giftCopy = gift.makeCopy()
        val giftStr = giftCopy.listResources
        val worth = EvaluateResourcesA(participator, giftCopy).run() + 0.8
        val disposition = participator.relationCenter.getNormalizedRelation(initiator)
        val acceptanceChance = disposition * worth.pow(2)

        return acceptanceChance.chanceOf<InteractionResult> {
            ReceiveGroupWideResourcesA(participator, gift.extract(participator.populationCenter.taker)).run()

            ProcessResult(Cooperation of "${participator.name} accepted a gift of $giftStr from ${initiator.name}") +
                    ProcessResult(makeResourcePackMemes(giftCopy)) +
                    ChangeRelationsI(initiator, participator, 2.0).run() to
                    ProcessResult(makeResourcePackMemes(giftCopy))
        } ?: (ProcessResult(
                Conflict of "${participator.name} rejected a gift of $giftStr from ${initiator.name} because " +
                        "of low disposition $disposition"
        ) + ChangeRelationsI(initiator, participator, -2.0).run() to emptyProcessResult)
    }
}
