package shmp.simulation.culture.group.process.interaction

import shmp.random.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.HelpEvent
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.toPositiveChange
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.action.*
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.thinking.meaning.makeResourcePackMemes
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import shmp.simulation.space.resource.container.ResourcePromise
import shmp.simulation.space.resource.container.ResourcePromisePack
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
            ChangeRelationsA(initiator, participator, request.evaluator.evaluate(given) / request.ceiling).run()

            ProcessResult(HelpEvent(
                    "${initiator.name} got help in $request from ${participator.name}: " +
                            "${given.listResources}, with delay ${delivery.delay}",
                    request.evaluator.evaluate(given)
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
        val acceptanceChance = participator.relationCenter.getNormalizedRelation(initiator) * worth.pow(2)

        return if (testProbability(acceptanceChance, Controller.session.random)) {
            ReceiveGroupWideResourcesA(participator, gift.extract()).run()

            ProcessResult(Event(
                    Type.Cooperation,
                    "${participator.name} accepted a gift of $giftStr from ${initiator.name}"
            ))  + ProcessResult(makeResourcePackMemes(giftCopy))
                    ChangeRelationsI(initiator, participator, 2.0).run() to
                            ProcessResult(makeResourcePackMemes(giftCopy))
        } else
            ProcessResult(Event(
                    Type.Conflict,
                    "${participator.name} rejected a gift of $giftStr from ${initiator.name}"
            )) +
                    ChangeRelationsI(initiator, participator, -2.0).run() to
                    emptyProcessResult
    }
}