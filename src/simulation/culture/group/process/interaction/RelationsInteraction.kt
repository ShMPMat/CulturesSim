package simulation.culture.group.process.interaction

import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.AddGroupA
import simulation.culture.group.process.action.ChangeRelationsA
import simulation.culture.group.process.action.ProcessGroupRemovalA
import kotlin.math.pow


class RelationsChangeInteraction(
        initiator: Group,
        participator: Group,
        private val delta: Double
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        ChangeRelationsA(participator, initiator, delta)
        ChangeRelationsA(initiator, participator, delta)

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
            RelationsChangeInteraction(initiator, participator, -0.1).run()
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
