package simulation.culture.group.process.interaction

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.DecideWarDeclarationA
import simulation.culture.group.process.action.GroupTransferA
import simulation.culture.group.process.action.pseudo.ActionSequencePA
import simulation.culture.group.process.action.pseudo.InteractionWrapperPA
import simulation.event.Event
import kotlin.math.pow


class GroupTransferWithNegotiationI(
        initiator: Group,
        participator: Group
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val relation = participator.relationCenter.getNormalizedRelation(initiator)
        if (!testProbability(relation.pow(2), session.random)) {
            ChangeRelationsI(initiator, participator, -1.0).run()

            val conflictEvents = if (DecideWarDeclarationA(participator, initiator).run()) {
                val decreaseRelations = InteractionWrapperPA(
                        ChangeRelationsI(initiator, participator, -10.0),
                        "${initiator.name} and ${participator.name} decreased their relations due to a battle"
                )

                ActionBattleI(
                        initiator,
                        participator,
                        ActionSequencePA(GroupTransferA(initiator, participator)),
                        decreaseRelations,
                        decreaseRelations
                ).run()
            }
            else listOf()

            return listOf(Event(
                    Event.Type.GroupInteraction,
                    "Group ${participator.name} refused to join conglomerate ${initiator.parentGroup.name}"
            )) + conflictEvents
        }



        return GroupTransferA(initiator, participator).run()
    }
}
