package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.ChangeRelationsA
import simulation.event.Event
import simulation.event.Type


class ChangeRelationsI(
        initiator: Group,
        participator: Group,
        private val delta: Double
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): ProcessResult {
        ChangeRelationsA(participator, initiator, delta).run()
        ChangeRelationsA(initiator, participator, delta).run()

        val relationTo = initiator.relationCenter.getNormalizedRelation(participator)
        val relationFrom = participator.relationCenter.getNormalizedRelation(initiator)
        return ProcessResult(Event(
                Type.GroupInteraction,
                "Groups ${initiator.name} and ${participator.name} changed their relations by $delta " +
                        "to the general of $relationTo and $relationFrom"
        ))
    }
}
