package shmp.simulation.culture.group.process.interaction

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.makeNegativeChange
import shmp.simulation.culture.group.centers.makePositiveChange
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.action.DecideWarDeclarationA
import shmp.simulation.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import shmp.simulation.culture.group.process.action.pseudo.InteractionWrapperPA
import shmp.simulation.culture.group.process.behaviour.WarB
import shmp.simulation.event.Event
import shmp.simulation.event.Type


class ProbableStrikeWarI(
        initiator: Group,
        participator: Group,
        val reason: String,
        val firstAction: EventfulGroupPseudoAction = makeDecreaseRelationsResult(initiator, participator),
        val secondAction: EventfulGroupPseudoAction = makeDecreaseRelationsResult(initiator, participator),
        val drawAction: EventfulGroupPseudoAction = makeDecreaseRelationsResult(initiator, participator)
) : AbstractGroupInteraction(initiator, participator) {
    var warStruck = false
        private set

    override fun innerRun(): InteractionResult =
            if (DecideWarDeclarationA(participator, initiator).run()) {
                val decreaseRelationsEvent = ChangeRelationsI(initiator, participator, -1.0).run()
                initiator.processCenter.addBehaviour(WarB(
                        participator,
                        firstAction,
                        secondAction,
                        drawAction
                ))

                warStruck = true

                decreaseRelationsEvent +
                        ProcessResult(Event(
                                Type.Conflict,
                                "${initiator.name} started a war with ${participator.name}, because $reason"
                        )) +
                        ProcessResult(makeNegativeChange(Trait.Peace)) to
                        ProcessResult(makeNegativeChange(Trait.Peace))
            } else ProcessResult(makePositiveChange(Trait.Peace)) to
                    ProcessResult(makePositiveChange(Trait.Peace))
}

fun makeDecreaseRelationsResult(initiator: Group, participator: Group) = InteractionWrapperPA(
        ChangeRelationsI(initiator, participator, -10.0),
        "${initiator.name} and ${participator.name} decreased their relations due to a war"
)
