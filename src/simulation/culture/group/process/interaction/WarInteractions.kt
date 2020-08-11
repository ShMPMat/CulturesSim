package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.DecideWarDeclarationA
import simulation.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import simulation.culture.group.process.action.pseudo.InteractionWrapperPA
import simulation.culture.group.process.behaviour.WarB
import simulation.event.Event
import simulation.event.Type


class ProbableStrikeWarI(
        initiator: Group,
        participator: Group,
        val reason: String,
        val firstAction: EventfulGroupPseudoAction = makeDecreaseRelationsWarResult(initiator, participator),
        val secondAction: EventfulGroupPseudoAction = makeDecreaseRelationsWarResult(initiator, participator),
        val drawAction: EventfulGroupPseudoAction = makeDecreaseRelationsWarResult(initiator, participator)
) : AbstractGroupInteraction(initiator, participator) {
    var warStruck = false
        private set

    override fun run(): List<Event> = if (DecideWarDeclarationA(participator, initiator).run()) {
        val decreaseRelationsEvent = ChangeRelationsI(initiator, participator, -1.0).run()
        initiator.processCenter.addBehaviour(WarB(
                participator,
                firstAction,
                secondAction,
                drawAction
        ))

        warStruck = true

        decreaseRelationsEvent + listOf(Event(
                Type.Conflict,
                "${initiator.name} started a war with ${participator.name}, because $reason"
        ))
    } else listOf()
}

fun makeDecreaseRelationsWarResult(initiator: Group, participator: Group) = InteractionWrapperPA(
        ChangeRelationsI(initiator, participator, -10.0),
        "${initiator.name} and ${participator.name} decreased their relations due to a war"
)
