package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.Trait
import simulation.culture.group.centers.makeNegativeChange
import simulation.culture.group.centers.makePositiveChange
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.GroupTransferA
import simulation.culture.group.process.action.pseudo.ActionSequencePA
import simulation.event.Event
import simulation.event.Type


class GroupTransferWithNegotiationI(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): ProcessResult {
        val strikeWarInteraction = ProbableStrikeWarI(
                initiator,
                participator,
                "${participator.name} wants to leave the Conglomerate",
                ActionSequencePA(GroupTransferA(initiator, participator))
        )
        val strikeWarResult = strikeWarInteraction.run()

        val transferResult =
                if (strikeWarInteraction.warStruck)
                    ProcessResult(Event(
                            Type.GroupInteraction,
                            "Group ${participator.name} objects joining " +
                                    "the conglomerate ${initiator.parentGroup.name}"
                    )) + ProcessResult(makeNegativeChange(Trait.Peace))
                else ProcessResult(makePositiveChange(Trait.Peace))

        return strikeWarResult + transferResult + GroupTransferA(initiator, participator).run()
    }
}
