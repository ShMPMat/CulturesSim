package shmp.simulation.culture.group.process.interaction

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.makeNegativeChange
import shmp.simulation.culture.group.centers.makePositiveChange
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.action.GroupTransferA
import shmp.simulation.culture.group.process.action.pseudo.ActionSequencePA
import shmp.simulation.event.Event
import shmp.simulation.event.Type


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
                            ProcessResult(makeNegativeChange(Trait.Peace)) to
                            ProcessResult(makeNegativeChange(Trait.Peace))
                else ProcessResult(makePositiveChange(Trait.Peace)) to
                        ProcessResult(makePositiveChange(Trait.Peace))

        return strikeWarResult + transferResultIni + GroupTransferA(initiator, participator).run() to
                transferResultPart
    }
}
