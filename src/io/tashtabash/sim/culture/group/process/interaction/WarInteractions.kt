package io.tashtabash.sim.culture.group.process.interaction

import io.tashtabash.sim.culture.group.ConflictResultEvent
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.toNegativeChange
import io.tashtabash.sim.culture.group.centers.toPositiveChange
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.action.DecideWarDeclarationA
import io.tashtabash.sim.culture.group.process.action.pseudo.ActionSequencePA
import io.tashtabash.sim.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import io.tashtabash.sim.culture.group.process.action.pseudo.InteractionWrapperPA
import io.tashtabash.sim.culture.group.process.action.pseudo.decide
import io.tashtabash.sim.culture.group.process.behaviour.Finish
import io.tashtabash.sim.culture.group.process.behaviour.WarB
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.event.Type


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

                warStruck = true

                decreaseRelationsEvent +
                    ProcessResult(Event(
                                Type.Conflict,
                                "${initiator.name} started a war with ${participator.name}, because $reason"
                        )) +
                        ProcessResult(Trait.Peace.toNegativeChange()) +
                        ProcessResult(WarB(participator, firstAction, secondAction, drawAction)) to
                        ProcessResult(Trait.Peace.toNegativeChange())
            } else ProcessResult(Trait.Peace.toPositiveChange()) to
                    ProcessResult(Trait.Peace.toPositiveChange())
}

class EndWarI(
        initiator: Group,
        participator: Group,
        private val initiatorWinAction: EventfulGroupPseudoAction,
        private val participatorWinAction: EventfulGroupPseudoAction,
        private val drawWinAction: EventfulGroupPseudoAction = ActionSequencePA(),
        private val warStatus: Finish
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        if (participator.state == Group.State.Dead || initiator.state == Group.State.Dead)
            return emptyProcessResult to emptyProcessResult

        val traitChange = warStatus.winner.decide(
                ProcessResult(Trait.Peace.toNegativeChange() * 2.0),
                emptyProcessResult,
                ProcessResult(Trait.Peace.toNegativeChange())
        )
        val winner = warStatus.winner.decide(initiator.name, participator.name, "no one")
        val action = warStatus.winner.decide(initiatorWinAction, participatorWinAction, drawWinAction)
        val actionInternalEvents = action.run()

        warStatus.winner.decide(initiator, participator, null)?.populationCenter?.stratumCenter?.warriorStratum
                ?.let { it.importance += 10 }
        warStatus.winner.decide(participator, initiator, null)?.populationCenter?.stratumCenter?.warriorStratum
                ?.let { it.importance += 3 }

        return actionInternalEvents + traitChange + ProcessResult(ConflictResultEvent(
                "The war between ${initiator.name} and ${participator.name} has ended, " +
                        "the winner is $winner, the result: $action",
                warStatus.winner
        )) to emptyProcessResult
    }
}

fun makeDecreaseRelationsResult(initiator: Group, participator: Group) = InteractionWrapperPA(
        ChangeRelationsI(initiator, participator, -10.0),
        "${initiator.name} and ${participator.name} decreased their relations due to a war"
)
