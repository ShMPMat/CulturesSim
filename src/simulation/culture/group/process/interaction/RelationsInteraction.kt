package simulation.culture.group.process.interaction

import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.*
import simulation.culture.group.request.Request
import kotlin.math.pow


class ChangeRelationsInteraction(
        initiator: Group,
        participator: Group,
        private val delta: Double
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        ChangeRelationsA(participator, initiator, delta).run()
        ChangeRelationsA(initiator, participator, delta).run()

        val relationTo = initiator.relationCenter.getNormalizedRelation(participator)
        val relationFrom = participator.relationCenter.getNormalizedRelation(initiator)
        return listOf(Event(
                Event.Type.GroupInteraction,
                "Groups ${initiator.name} and ${participator.name} improved their relations by $delta " +
                        "to the general of $relationTo and $relationFrom"
        ))
    }
}


class GroupTransferInteraction(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val relation = participator.relationCenter.getNormalizedRelation(initiator)
        if (!testProbability(relation.pow(2), Controller.session.random)) {
            ChangeRelationsInteraction(initiator, participator, -1.0).run()
            return listOf(Event(
                    Event.Type.GroupInteraction,
                    "Group ${participator.name} refused to join conglomerate ${initiator.parentGroup.name}"
            ))
        }

        AddGroupA(initiator, participator).run()
        ProcessGroupRemovalA(participator, participator).run()

        return listOf(Event(
                Event.Type.GroupInteraction,
                "Group ${participator.name} joined to conglomerate ${initiator.parentGroup.name}"
        ))
    }
}

class RequestHelpInteraction(
        initiator: Group,
        participator: Group,
        val request: Request
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        if (!GrantHelpA(participator, initiator, 0.5).run())
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

            listOf(Event(
                    Event.Type.GroupInteraction,
                    "${initiator.name} got help in $request from ${participator.name}: " +
                            "${given.listResources}, with delay ${delivery.delay}",
                    "value", request.evaluator.evaluate(given)
            ))
        } else emptyList()
    }
}
