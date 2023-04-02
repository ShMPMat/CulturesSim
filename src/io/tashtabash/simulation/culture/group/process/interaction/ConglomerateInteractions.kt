package io.tashtabash.simulation.culture.group.process.interaction

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.centers.Trait
import io.tashtabash.simulation.culture.group.centers.toNegativeChange
import io.tashtabash.simulation.culture.group.centers.toPositiveChange
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.group.process.action.GroupTransferA
import io.tashtabash.simulation.culture.group.process.action.pseudo.ActionSequencePA
import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.event.Type


class GroupTransferWithNegotiationI(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        val strikeWarInteraction = ProbableStrikeWarI(
                initiator,
                participator,
                "${participator.name} wants to leave the Conglomerate",
                ActionSequencePA(GroupTransferA(initiator, participator))
        )
        val strikeWarResult = strikeWarInteraction.run()

        val (transferResultIni, transferResultPart) =
                if (strikeWarInteraction.warStruck)
                    ProcessResult(Event(
                            Type.GroupInteraction,
                            "Group ${participator.name} objects joining " +
                                    "the conglomerate ${initiator.parentGroup.name}"
                    )) +
                            ProcessResult(Trait.Peace.toNegativeChange()) to
                            ProcessResult(Trait.Peace.toNegativeChange())
                else ProcessResult(Trait.Peace.toPositiveChange()) to
                        ProcessResult(Trait.Peace.toPositiveChange())

        return strikeWarResult + transferResultIni + GroupTransferA(initiator, participator).run() to
                transferResultPart
    }
}
