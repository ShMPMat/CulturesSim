package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.DecideBattleTileA
import simulation.culture.group.process.action.GatherWarriorsA
import simulation.culture.group.process.action.pseudo.ActionSequence
import simulation.culture.group.process.action.pseudo.BattlePA
import simulation.culture.group.process.action.pseudo.GroupPseudoAction
import simulation.event.Event


class BattleI(initiator: Group, participator: Group): AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<BattleResultEvent> {
        val tile = DecideBattleTileA(initiator, participator)
        val iniEvaluation = evaluateForces(participator)
        val partEvaluation = evaluateForces(initiator)
        val iniWarriors = GatherWarriorsA(initiator, iniEvaluation).run()
        val partWarriors = GatherWarriorsA(participator, partEvaluation).run()

        val result = BattlePA(iniWarriors, partWarriors).run()

        val description = when (result) {
            BattlePA.Winner.First -> "${initiator.name} won a battle with ${participator.name} on $tile"
            BattlePA.Winner.Second -> "${participator.name} won a battle with ${initiator.name} on $tile"
            BattlePA.Winner.Draw -> "Not ${initiator.name} nor ${participator.name} won in a battle on $tile"
        }

        return listOf(BattleResultEvent(description, result))
    }

    private fun evaluateForces(group: Group) =
            (group.populationCenter.stratumCenter.warriorStratum.cumulativeWorkAblePopulation + 10) * 2
}

class ActionBattleI(
        initiator: Group,
        participator: Group,
        val initiatorWinAction: GroupPseudoAction,
        val participatorWinAction: GroupPseudoAction,
        val drawWinAction: GroupPseudoAction = ActionSequence(listOf())
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): List<Event> {
        val resultEvents = BattleI(initiator, participator).run()
        val action = when (resultEvents[0].status) {
            BattlePA.Winner.First -> initiatorWinAction
            BattlePA.Winner.Second -> participatorWinAction
            BattlePA.Winner.Draw -> drawWinAction
        }
        action.run()

        val actionEvent = Event(
                Event.Type.Change,
                "In the result of battle between ${initiator.name} and ${participator.name}: $action"
        )
        return resultEvents + listOf(actionEvent)
    }
}


class BattleResultEvent(
        description: String,
        val status: BattlePA.Winner
): Event(Type.Conflict, description, "status", status)
