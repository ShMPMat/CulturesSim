package io.tashtabash.simulation.culture.group.process.interaction

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.centers.Trait
import io.tashtabash.simulation.culture.group.centers.toNegativeChange
import io.tashtabash.simulation.culture.group.centers.toPositiveChange
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.group.process.action.DecideWarDeclarationA
import io.tashtabash.simulation.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import io.tashtabash.simulation.culture.group.process.action.pseudo.InteractionWrapperPA
import io.tashtabash.simulation.culture.group.process.behaviour.WarB
import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.event.Type


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
                        ProcessResult(Trait.Peace.toNegativeChange()) to
                        ProcessResult(Trait.Peace.toNegativeChange())
            } else ProcessResult(Trait.Peace.toPositiveChange()) to
                    ProcessResult(Trait.Peace.toPositiveChange())
}

fun makeDecreaseRelationsResult(initiator: Group, participator: Group) = InteractionWrapperPA(
        ChangeRelationsI(initiator, participator, -10.0),
        "${initiator.name} and ${participator.name} decreased their relations due to a war"
)
