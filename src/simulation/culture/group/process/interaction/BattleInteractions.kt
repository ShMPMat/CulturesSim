package simulation.culture.group.process.interaction

import simulation.culture.group.ConflictResultEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.DecideBattleTileA
import simulation.culture.group.process.action.GatherWarriorsA
import simulation.culture.group.process.action.pseudo.*
import simulation.event.Event
import simulation.event.Type


class BattleI(initiator: Group, participator: Group): AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<ConflictResultEvent> {
        val tile = DecideBattleTileA(initiator, participator).run().posStr
        val iniEvaluation = evaluateForces(participator)
        val partEvaluation = evaluateForces(initiator)
        val iniWarriors = GatherWarriorsA(initiator, iniEvaluation).run()
        val partWarriors = GatherWarriorsA(participator, partEvaluation).run()

        val result = BattlePA(iniWarriors, partWarriors).run()

        val description = when (result) {
            ConflictWinner.First -> "${initiator.name} won a battle with ${participator.name} on $tile"
            ConflictWinner.Second -> "${participator.name} won a battle with ${initiator.name} on $tile"
            ConflictWinner.Draw -> "Not ${initiator.name} nor ${participator.name} won in a battle on $tile"
        }

        return listOf(ConflictResultEvent(description, result))
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
    override fun run(): List<Event> {
        val resultEvents = BattleI(initiator, participator).run()
        val action = resultEvents[0].status.decide(initiatorWinAction, participatorWinAction, drawWinAction)
        val actionInternalEvents = action.run()

        val actionEvent = Event(
                Type.Change,
                "In the result of battle between ${initiator.name} and ${participator.name}: $action"
        )
        return resultEvents + actionInternalEvents + listOf(actionEvent)
    }
}
