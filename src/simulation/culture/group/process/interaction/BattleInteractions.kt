package simulation.culture.group.process.interaction

import simulation.culture.group.ConflictResultEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.DecideBattleTileA
import simulation.culture.group.process.action.GatherWarriorsA
import simulation.culture.group.process.action.pseudo.*
import simulation.event.Event
import simulation.event.Type


class BattleI(initiator: Group, participator: Group): AbstractGroupInteraction(initiator, participator) {
    var status = ConflictWinner.Draw
        private set

    override fun run(): ProcessResult {
        val tile = DecideBattleTileA(initiator, participator).run().posStr
        val iniEvaluation = evaluateForces(participator)
        val partEvaluation = evaluateForces(initiator)
        val iniWarriors = GatherWarriorsA(initiator, iniEvaluation).run()
        val partWarriors = GatherWarriorsA(participator, partEvaluation).run()

        status = BattlePA(iniWarriors, partWarriors).run()

        val description = when (status) {
            ConflictWinner.First -> "${initiator.name} won a battle with ${participator.name} on $tile"
            ConflictWinner.Second -> "${participator.name} won a battle with ${initiator.name} on $tile"
            ConflictWinner.Draw -> "Not ${initiator.name} nor ${participator.name} won in a battle on $tile"
        }

        return ProcessResult(ConflictResultEvent(description, status))
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
    override fun run(): ProcessResult {
        val battle = BattleI(initiator, participator)
        val resultEvents = battle.run()
        val action = battle.status.decide(initiatorWinAction, participatorWinAction, drawWinAction)
        val actionInternalEvents = action.run()

        val actionEvent = Event(
                Type.Change,
                "In the result of battle between ${initiator.name} and ${participator.name}: $action"
        )
        return resultEvents + actionInternalEvents + ProcessResult(actionEvent)
    }
}
