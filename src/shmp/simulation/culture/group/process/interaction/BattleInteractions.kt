package shmp.simulation.culture.group.process.interaction

import shmp.simulation.culture.group.ConflictResultEvent
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.toNegativeChange
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.action.DecideBattleTileA
import shmp.simulation.culture.group.process.action.GatherWarriorsA
import shmp.simulation.culture.group.process.action.pseudo.*
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.culture.thinking.meaning.makeStratumMemes
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import kotlin.math.pow


class BattleI(initiator: Group, participator: Group) : AbstractGroupInteraction(initiator, participator) {
    var status = ConflictWinner.Draw
        private set

    override fun innerRun(): InteractionResult {
        val tile = DecideBattleTileA(initiator, participator).run().posStr
        val iniEvaluation = evaluateForces(participator) *
                (1 - initiator.cultureCenter.traitCenter.processedValue(Trait.Peace)).pow(3) *
                initiator.populationCenter.stratumCenter.warriorStratum.effectiveness
        val partEvaluation = evaluateForces(initiator) *
                (1 - initiator.cultureCenter.traitCenter.processedValue(Trait.Peace)).pow(3) *
                participator.populationCenter.stratumCenter.warriorStratum.effectiveness
        val iniWarriors = GatherWarriorsA(initiator, iniEvaluation).run()
        val partWarriors = GatherWarriorsA(participator, partEvaluation).run()

        if (initiator.populationCenter.stratumCenter.warriorStratum.effectiveness > 1 || participator.populationCenter.stratumCenter.warriorStratum.effectiveness > 1) {
            val g = 7
        }

        status = BattlePA(iniWarriors, partWarriors).run()

        val description = status.decide(
                "${initiator.name} won a battle with ${participator.name} on $tile",
                "${participator.name} won a battle with ${initiator.name} on $tile",
                "Not ${initiator.name} nor ${participator.name} won in a battle on $tile"
        )
        val traitChangeIniResult = status.decide(
                ProcessResult(Trait.Peace.toNegativeChange()),
                emptyProcessResult,
                ProcessResult(Trait.Peace.toNegativeChange())
        )
        val traitChangePartResult = status.decide(
                emptyProcessResult,
                ProcessResult(Trait.Peace.toNegativeChange()),
                ProcessResult(Trait.Peace.toNegativeChange())
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
