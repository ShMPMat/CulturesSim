package simulation.culture.group.process.interaction

import simulation.culture.group.ConflictResultEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.centers.Trait
import simulation.culture.group.centers.makeNegativeChange
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.DecideBattleTileA
import simulation.culture.group.process.action.GatherWarriorsA
import simulation.culture.group.process.action.pseudo.*
import simulation.culture.group.process.emptyProcessResult
import simulation.culture.thinking.meaning.makeStratumMemes
import simulation.event.Event
import simulation.event.Type


class BattleI(initiator: Group, participator: Group) : AbstractGroupInteraction(initiator, participator) {
    var status = ConflictWinner.Draw
        private set

    override fun innerRun(): InteractionResult {
        val tile = DecideBattleTileA(initiator, participator).run().posStr
        val iniEvaluation = evaluateForces(participator)
        val partEvaluation = evaluateForces(initiator)
        val iniWarriors = GatherWarriorsA(initiator, iniEvaluation).run()
        val partWarriors = GatherWarriorsA(participator, partEvaluation).run()

        status = BattlePA(iniWarriors, partWarriors).run()

        val description = status.decide(
                "${initiator.name} won a battle with ${participator.name} on $tile",
                "${participator.name} won a battle with ${initiator.name} on $tile",
                "Not ${initiator.name} nor ${participator.name} won in a battle on $tile"
        )
        val traitChangeIniResult = status.decide(
                ProcessResult(makeNegativeChange(Trait.Peace)),
                emptyProcessResult,
                ProcessResult(makeNegativeChange(Trait.Peace))
        )
        val traitChangePartResult = status.decide(
                emptyProcessResult,
                ProcessResult(makeNegativeChange(Trait.Peace)),
                ProcessResult(makeNegativeChange(Trait.Peace))
        )
        val warriorIniMemes = iniWarriors
                .map { makeStratumMemes(it.stratum) }
                .flatten()
        val warriorPartMemes = partWarriors
                .map { makeStratumMemes(it.stratum) }
                .flatten()
        return ProcessResult(ConflictResultEvent(description, status)) +
                ProcessResult(warriorIniMemes) +
                traitChangeIniResult to
                ProcessResult(warriorPartMemes) +
                traitChangePartResult
    }

    private fun evaluateForces(group: Group) =
            (group.populationCenter.stratumCenter.warriorStratum.cumulativeWorkAblePopulation + 10) * 2
}

class ActionBattleI(
        initiator: Group,
        participator: Group,
        private val initiatorWinAction: EventfulGroupPseudoAction,
        private val participatorWinAction: EventfulGroupPseudoAction,
        private val drawWinAction: EventfulGroupPseudoAction = ActionSequencePA()
) : AbstractGroupInteraction(initiator, participator) {
    override fun innerRun(): InteractionResult {
        val battle = BattleI(initiator, participator)
        val resultIni = battle.run()
        val action = battle.status.decide(initiatorWinAction, participatorWinAction, drawWinAction)
        val actionInternalEvents = action.run()

        val actionEvent = Event(
                Type.Change,
                "In the result of battle between ${initiator.name} and ${participator.name}: $action"
        )
        return resultIni + actionInternalEvents + ProcessResult(actionEvent) to
                emptyProcessResult
    }
}
