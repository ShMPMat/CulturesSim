package io.tashtabash.simulation.culture.group.process.behaviour

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.simulation.culture.group.ConflictResultEvent
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.centers.Trait
import io.tashtabash.simulation.culture.group.centers.toNegativeChange
import io.tashtabash.simulation.culture.group.process.*
import io.tashtabash.simulation.culture.group.process.action.AddGroupA
import io.tashtabash.simulation.culture.group.process.action.ChooseResourcesAndTakeA
import io.tashtabash.simulation.culture.group.process.action.TestTraitA
import io.tashtabash.simulation.culture.group.process.action.pseudo.ActionSequencePA
import io.tashtabash.simulation.culture.group.process.action.pseudo.ConflictWinner
import io.tashtabash.simulation.culture.group.process.action.pseudo.ConflictWinner.*
import io.tashtabash.simulation.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import io.tashtabash.simulation.culture.group.process.action.pseudo.decide
import io.tashtabash.simulation.culture.group.process.interaction.BattleI
import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.event.Type
import io.tashtabash.simulation.space.resource.container.ResourcePromisePack
import kotlin.math.pow
import kotlin.math.sqrt


object RandomWarB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val groups = group.relationCenter.relatedGroups.sortedBy { it.name }

        val opponent = groups.randomElementOrNull {
            val relation = group.relationCenter.getNormalizedRelation(it)
            val warPower = it.populationCenter.stratumCenter.warriorStratum.cumulativeWorkAblePopulation
            (1 - relation.pow(2)) / (warPower + 1)
        } ?: return emptyProcessResult

        val goal =
                if (opponent.parentGroup != group.parentGroup &&
                        TestTraitA(group, Trait.Expansion.getPositive().pow(0.5)).run())
                    AddGroupA(opponent, group)
                else ChooseResourcesAndTakeA(
                        group,
                        ResourcePromisePack(opponent.populationCenter.turnResources),
                        1000
                )

        group.processCenter.addBehaviour(WarB(
                opponent,
                ActionSequencePA(goal),
                ActionSequencePA(ChooseResourcesAndTakeA(
                        opponent,
                        ResourcePromisePack(group.populationCenter.turnResources),
                        1000
                ))
        ))

        return ProcessResult(Event(
                Type.Conflict,
                "${group.name} declared war to ${opponent.name}"
        )) +
                ProcessResult(Trait.Peace.toNegativeChange() * 2.0)
    }

    override val internalToString = "Declare war to a random neighbour"
}


class WarB(
        val opponent: Group,
        private val initiatorWinAction: EventfulGroupPseudoAction,
        private val participatorWinAction: EventfulGroupPseudoAction,
        private val drawWinAction: EventfulGroupPseudoAction = ActionSequencePA(),
        val warFinisher: WarFinisher = ProbabilisticWarFinisher()
) : PlanBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (isFinished)
            return emptyProcessResult

        val battle = BattleI(group, opponent)
        val result = battle.run()
        val battleResult = battle.status

        val warResult = when (val warStatus = warFinisher.decide(listOf(battleResult))) {
            Continue -> emptyProcessResult
            is Finish -> {
                if (opponent.state == Group.State.Dead || group.state == Group.State.Dead)
                    return emptyProcessResult

                val traitChange = warStatus.winner.decide(
                        ProcessResult(Trait.Peace.toNegativeChange() * 2.0),
                        emptyProcessResult,
                        ProcessResult(Trait.Peace.toNegativeChange())
                )
                val winner = warStatus.winner.decide(group.name, opponent.name, "no one")
                val action = warStatus.winner.decide(initiatorWinAction, participatorWinAction, drawWinAction)
                val actionInternalEvents = action.run()

                warStatus.winner.decide(group, opponent, null)?.populationCenter?.stratumCenter?.warriorStratum
                        ?.let { it.importance += 10 }
                warStatus.winner.decide(opponent, group, null)?.populationCenter?.stratumCenter?.warriorStratum
                        ?.let { it.importance += 3 }

                isFinished = true

                actionInternalEvents + traitChange +
                        ProcessResult(ConflictResultEvent(
                                "The war between ${group.name} and ${opponent.name} has ended, " +
                                        "the winner is $winner, the result: $action",
                                warStatus.winner
                        ))
            }
        }

        return result + warResult
    }

    override val internalToString = "Carry on war with ${opponent.name}"
}


interface WarFinisher {
    fun decide(results: List<ConflictWinner>): WarStatus
}

class ProbabilisticWarFinisher : WarFinisher {
    private var first = 0.0
    private var second = 0.0
    private var draw = 0.0

    private fun incF() = first++
    private fun incS() = second++
    private fun incD() = draw++

    override fun decide(results: List<ConflictWinner>): WarStatus {
        results.forEach {
            it.decide(this::incF, this::incS, this::incD)()
        }

        return listOf(
                Finish(First) to first,
                Finish(Second) to second,
                Finish(Draw) to draw,
                Continue to sqrt(first + second + draw + 1)
        ).randomElement { (_, n) -> n }.first
    }
}


sealed class WarStatus

object Continue : WarStatus()
class Finish(val winner: ConflictWinner) : WarStatus()
