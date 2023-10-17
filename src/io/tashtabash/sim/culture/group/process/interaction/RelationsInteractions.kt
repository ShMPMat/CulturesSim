package io.tashtabash.sim.culture.group.process.interaction

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.action.ChangeRelationsA
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.event.IntergroupInteraction


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
                IntergroupInteraction,
                "Groups ${initiator.name} and ${participator.name} changed their relations by $delta " +
                        "to the general of $relationTo and $relationFrom"
        )) to
                emptyProcessResult
    }
}
