package simulation.culture.group.process.interaction

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.AddGroupA
import simulation.culture.group.process.action.ProcessGroupRemovalA
import simulation.event.Event
import kotlin.math.pow


class GroupTransferI(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val relation = participator.relationCenter.getNormalizedRelation(initiator)
        if (!testProbability(relation.pow(2), Controller.session.random)) {
            ChangeRelationsI(initiator, participator, -1.0).run()
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
