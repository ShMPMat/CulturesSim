package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.GroupTransferA
import simulation.culture.group.process.action.pseudo.ActionSequencePA
import simulation.event.Event
import simulation.event.Type
import kotlin.math.pow


class GroupTransferWithNegotiationI(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val strikeWarInteraction = ProbableStrikeWarI(
                initiator,
                participator,
                "${participator.name} wants to leave the Conglomerate",
                ActionSequencePA(GroupTransferA(initiator, participator))
        )
        val strikeWarResult = strikeWarInteraction.run()

        val transferResultEvents =
                if (strikeWarInteraction.warStruck)
                    listOf(Event(
                            Type.GroupInteraction,
                            "Group ${participator.name} objects joining " +
                                    "the conglomerate ${initiator.parentGroup.name}"
                    ))
                else listOf()

        return strikeWarResult + transferResultEvents + GroupTransferA(initiator, participator).run()
    }
}
