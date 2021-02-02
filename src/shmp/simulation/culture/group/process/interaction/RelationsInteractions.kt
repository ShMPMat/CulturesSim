package shmp.simulation.culture.group.process.interaction

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.action.ChangeRelationsA
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.event.Event
import shmp.simulation.event.Type


class ChangeRelationsI(
        initiator: Group,
        participator: Group,
        private val delta: Double
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        ChangeRelationsA(participator, initiator, delta).run()
        ChangeRelationsA(initiator, participator, delta).run()

        val relationTo = initiator.relationCenter.getNormalizedRelation(participator)
        val relationFrom = participator.relationCenter.getNormalizedRelation(initiator)
        return ProcessResult(Event(
                Type.GroupInteraction,
                "Groups ${initiator.name} and ${participator.name} changed their relations by $delta " +
                        "to the general of $relationTo and $relationFrom"
        )) to
                emptyProcessResult
    }
}
