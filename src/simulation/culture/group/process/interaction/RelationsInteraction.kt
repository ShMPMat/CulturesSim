package simulation.culture.group.process.interaction

import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.AddGroupA
import simulation.culture.group.process.action.ImproveRelationsA
import simulation.culture.group.process.action.ProcessGroupRemovalA


class RelationsImprovementInteraction(
        initiator: Group,
        participator: Group,
        val amount: Double
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        ImproveRelationsA(participator, initiator, amount)
        ImproveRelationsA(initiator, participator, amount)

        val relationTo = initiator.relationCenter.getNormalizedRelation(participator)
        val relationFrom = participator.relationCenter.getNormalizedRelation(initiator)
        return listOf(Event(
                Event.Type.GroupInteraction,
                "Groups ${initiator.name} and ${participator.name} improved their relations by $amount " +
                        "to the general of $relationTo and $relationFrom"
        ))
    }
}


class GroupTransferInteraction(
        initiator: Group,
        participator: Group
): AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        AddGroupA(initiator, participator).run()
        ProcessGroupRemovalA(participator, participator).run()
        return listOf(Event(
                Event.Type.GroupInteraction,
                "Group ${participator.name} joined to conglomerate ${initiator.parentGroup.name}"
        ))
    }
}
