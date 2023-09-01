package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.toNegativeChange
import io.tashtabash.sim.culture.group.process.*
import io.tashtabash.sim.culture.group.process.action.AddGroupA
import io.tashtabash.sim.culture.group.process.action.ChooseResourcesAndTakeA
import io.tashtabash.sim.culture.group.process.action.pseudo.ActionSequencePA
import io.tashtabash.sim.culture.group.process.action.pseudo.ConflictWinner
import io.tashtabash.sim.culture.group.process.action.pseudo.ConflictWinner.*
import io.tashtabash.sim.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import io.tashtabash.sim.culture.group.process.action.pseudo.decide
import io.tashtabash.sim.culture.group.process.action.testOn
import io.tashtabash.sim.culture.group.process.interaction.BattleI
import io.tashtabash.sim.culture.group.process.interaction.EndWarI
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.event.Type
import io.tashtabash.sim.space.resource.container.ResourcePromisePack
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
                if (opponent.parentGroup != group.parentGroup && Trait.Expansion.getPositive().pow(0.5) testOn group)
                    AddGroupA(opponent, group)
                else ChooseResourcesAndTakeA(
                        group,
                        ResourcePromisePack(opponent.populationCenter.turnResources),
                        1000
                )

        return ProcessResult(Event(Type.Conflict, "${group.name} declared war to ${opponent.name}")) +
                ProcessResult(Trait.Peace.toNegativeChange() * 2.0) +
                ProcessResult(WarB(
                        opponent,
                        ActionSequencePA(goal),
                        ActionSequencePA(ChooseResourcesAndTakeA(
                                opponent,
                                ResourcePromisePack(group.populationCenter.turnResources),
                                1000
                        ))
                ))
    }

    override val internalToString = "Declare war to a random neighbour"
}


class WarB(
        val opponent: Group,
        private val initiatorWinAction: EventfulGroupPseudoAction,
        private val participatorWinAction: EventfulGroupPseudoAction,
        private val drawWinAction: EventfulGroupPseudoAction = ActionSequencePA(),
        private val warFinisher: WarFinisher = ProbabilisticWarFinisher()
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
                isFinished = true
                EndWarI(group, opponent, initiatorWinAction, participatorWinAction, drawWinAction, warStatus)
                        .run()
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

    private fun incrementF() = first++
    private fun incrementS() = second++
    private fun incrementD() = draw++

    override fun decide(results: List<ConflictWinner>): WarStatus {
        results.forEach {
            it.decide(this::incrementF, this::incrementS, this::incrementD)()
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
