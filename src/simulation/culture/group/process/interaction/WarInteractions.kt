package simulation.culture.group.process.interaction

import shmp.random.testProbability
import simulation.Controller
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
        val probability: Double,
        val reason: String,
        val firstAction: EventfulGroupPseudoAction = makeDecreaseRelationsWarResult(initiator, participator),
        val secondAction: EventfulGroupPseudoAction = makeDecreaseRelationsWarResult(initiator, participator),
        val drawAction: EventfulGroupPseudoAction = makeDecreaseRelationsWarResult(initiator, participator)
) : AbstractGroupInteraction(initiator, participator) {
    var warStruck = false
        private set

    override fun run(): List<Event> {
        if (testProbability(probability, Controller.session.random))
            return emptyList()

        val decreaseRelationsEvent = ChangeRelationsI(initiator, participator, -1.0).run()

        val conflictEvents =
                if (DecideWarDeclarationA(participator, initiator).run()) {
                    initiator.processCenter.addBehaviour(WarB(
                            participator,
                            firstAction,
                            secondAction,
                            drawAction
                    ))

                    listOf(Event(
                            Type.Conflict,
                            "${initiator.name} started a war with ${participator.name}, because $reason"
                    ))
                } else listOf()

        warStruck = true

        return decreaseRelationsEvent + conflictEvents
    }
}

fun makeDecreaseRelationsWarResult(initiator: Group, participator: Group) = InteractionWrapperPA(
        ChangeRelationsI(initiator, participator, -10.0),
        "${initiator.name} and ${participator.name} decreased their relations due to a war"
)
