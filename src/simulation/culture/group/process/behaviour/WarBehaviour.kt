package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.ConflictResultEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.pseudo.ActionSequencePA
import simulation.culture.group.process.action.pseudo.ConflictWinner
import simulation.culture.group.process.action.pseudo.ConflictWinner.*
import simulation.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import simulation.culture.group.process.action.pseudo.decide
import simulation.culture.group.process.interaction.BattleI
import simulation.event.Event
import kotlin.math.sqrt


class WarB(
        val opponent: Group,
        private val initiatorWinAction: EventfulGroupPseudoAction,
        private val participatorWinAction: EventfulGroupPseudoAction,
        private val drawWinAction: EventfulGroupPseudoAction = ActionSequencePA(),
        val warFinisher: WarFinisher = ProbabilisticWarFinisher()
) : PlanBehaviour() {
    override fun run(group: Group): List<Event> {
        if (isFinished)
            return emptyList()

        val results = BattleI(group, opponent).run()
        val battleResults = results.map { it.status }

        val warStatusEvents = when (val warStatus = warFinisher.decide(battleResults)) {
            Continue -> emptyList()
            is Finish -> {
                val winner = warStatus.winner.decide(group.name, opponent.name, "no one")
                val action = warStatus.winner.decide(initiatorWinAction, participatorWinAction, drawWinAction)
                val actionInternalEvents = action.run()

                isFinished = true

                actionInternalEvents + listOf(ConflictResultEvent(
                        "The war between ${group.name} and ${opponent.name} has ended, " +
                                "the winner is $winner, the result: $action",
                        warStatus.winner
                ))
            }
        }

        return results + warStatusEvents
    }

    override val internalToString = "Carry on war with ${opponent.name}"
}


interface WarFinisher {
    fun decide(results: List<ConflictWinner>): WarStatus
}

class ProbabilisticWarFinisher(): WarFinisher {
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

        return randomElement(
                listOf(
                        Finish(First) to first,
                        Finish(Second) to second,
                        Finish(Draw) to draw,
                        Continue to sqrt(first + second + draw + 1)
                ),
                { (_, n) -> n },
                Controller.session.random
        ).first
    }
}


sealed class WarStatus

object Continue : WarStatus()
class Finish(val winner: ConflictWinner) : WarStatus()


