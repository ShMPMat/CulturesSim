package simulation.culture.group.process.action.pseudo

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.process.action.pseudo.ConflictWinner.*
import simulation.culture.group.stratum.WorkerBunch
import simulation.event.Event
import kotlin.math.min
import kotlin.math.pow


class BattlePA(val firstSide: List<WorkerBunch>, val secondSide: List<WorkerBunch>) : GroupPseudoAction {
    override fun run(): ConflictWinner {
        val firstSideForces = firstSide.getCumulativeForce()
        val secondSideForces = secondSide.getCumulativeForce()
        val drawChance = min(
                firstSideForces.pow(2) / (secondSideForces + 1),
                secondSideForces.pow(2) / (firstSideForces + 1)
        ) + 1.0

        try {
            return randomElement(
                    listOf(First to firstSideForces, Second to secondSideForces, Draw to drawChance),
                    { (_, n) -> n },
                    Controller.session.random
            ).first
        } catch (e: Exception) {
            return Draw
        }
    }

    private fun List<WorkerBunch>.getCumulativeForce() = this
            .map { it.cumulativeWorkers }
            .foldRight(0, Int::plus)
            .toDouble()

    override val internalToString = "A battle of ${firstSide.joinToString()} versus ${secondSide.joinToString()}"
}


enum class ConflictWinner { First, Second, Draw }

fun <E> ConflictWinner.decide(fst: E, snd: E, draw: E) = when (this) {
    First -> fst
    Second -> snd
    Draw -> draw
}
