package simulation.culture.group.process.interaction

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.HelpEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.*
import simulation.culture.group.request.Request
import simulation.event.Event
import simulation.space.resource.container.ResourcePromise
import simulation.space.resource.container.ResourcePromisePack
import kotlin.math.pow


class RequestHelpI(
        initiator: Group,
        participator: Group,
        val request: Request
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        if (!CooperateA(participator, initiator, 0.5).run())
            return emptyList()

        val given = ExecuteRequestA(participator, request.reassign(participator)).run()
        val delivery = ScheduleActionA(
                participator,
                ReceiveRequestResourcesA(initiator, request, given),
                ComputeTravelTime(participator, initiator).run()
        )

        return if (given.isNotEmpty) {
            delivery.run()
            ChangeRelationsA(initiator, participator, request.evaluator.evaluate(given) / request.ceiling).run()

            listOf(HelpEvent(
                    "${initiator.name} got help in $request from ${participator.name}: " +
                            "${given.listResources}, with delay ${delivery.delay}",
                    request.evaluator.evaluate(given)
            ))
        } else emptyList()
    }
}


class GiveGiftI(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val relation = initiator.relationCenter.getNormalizedRelation(participator)

        val gift = ChooseResourcesA(
                initiator,
                ResourcePromisePack(initiator.populationCenter.turnResources.resources.map { ResourcePromise(it) }),
                (1000 * (1 - relation) + 1).toInt()
        ).run()

        return ReceiveGiftI(initiator, participator, gift).run()
    }
}

class ReceiveGiftI(
        initiator: Group,
        participator: Group,
        val gift: ResourcePromisePack
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val giftCopy = gift.makeCopy()
        val giftStr = giftCopy.listResources
        val worth = EvaluateResourcesA(participator, giftCopy).run() + 0.8
        val acceptanceChance = participator.relationCenter.getNormalizedRelation(initiator) * worth.pow(2)

        return if (testProbability(acceptanceChance, Controller.session.random)) {
            ReceiveGroupWideResourcesA(participator, gift.extract()).run()

            listOf(Event(
                    Event.Type.Cooperation,
                    "${participator.name} accepted a gift of $giftStr from ${initiator.name}"
            )) +
                    ChangeRelationsI(initiator, participator, -2.0).run()
        } else
            listOf(Event(
                    Event.Type.Conflict,
                    "${participator.name} rejected a gift of $giftStr from ${initiator.name}"
            )) +
                    ChangeRelationsI(initiator, participator, -2.0).run()
    }
}
