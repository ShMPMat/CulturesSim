package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.Trait
import simulation.culture.group.centers.makeNegativeChange
import simulation.culture.group.centers.makePositiveChange
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.DecideWarDeclarationA
import simulation.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import simulation.culture.group.process.action.pseudo.InteractionWrapperPA
import simulation.culture.group.process.behaviour.WarB
import simulation.culture.group.process.emptyProcessResult
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

fun makeDecreaseRelationsWarResult(initiator: Group, participator: Group) = InteractionWrapperPA(
        ChangeRelationsI(initiator, participator, -10.0),
        "${initiator.name} and ${participator.name} decreased their relations due to a war"
)
