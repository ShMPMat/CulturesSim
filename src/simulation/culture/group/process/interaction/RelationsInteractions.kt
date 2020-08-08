package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.ChangeRelationsA
import simulation.event.Event
import simulation.event.Type


class ChangeRelationsI(
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
                Type.GroupInteraction,
                "Groups ${initiator.name} and ${participator.name} improved their relations by $delta " +
                        "to the general of $relationTo and $relationFrom"
        ))
    }
}
