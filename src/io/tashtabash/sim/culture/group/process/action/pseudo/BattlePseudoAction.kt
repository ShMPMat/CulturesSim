package io.tashtabash.sim.culture.group.process.action.pseudo

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.sim.culture.group.process.action.pseudo.ConflictWinner.*
import io.tashtabash.sim.culture.group.stratum.StratumPeople
import kotlin.math.min
import kotlin.math.pow


class BattlePA(val firstSide: List<StratumPeople>, val secondSide: List<StratumPeople>) : GroupPseudoAction {
    override fun run(): ConflictWinner {
        val firstSideForces = firstSide.getCumulativeForce()
        val secondSideForces = secondSide.getCumulativeForce()
        val drawChance = min(
                firstSideForces.pow(2) / (secondSideForces + 1),
                secondSideForces.pow(2) / (firstSideForces + 1)
        ) + 1.0

        val result = listOf(First to firstSideForces, Second to secondSideForces, Draw to drawChance)
                .randomElement { (_, n) -> n }
                .first

        result.decide(
                {
                    mildLoss(firstSide)
                    bigLoss(secondSide)
                },
                {
                    bigLoss(firstSide)
                    mildLoss(secondSide)
                },
                {
                    bigLoss(firstSide)
                    bigLoss(secondSide)
                }
        )()

        return result
    }

    private fun List<StratumPeople>.getCumulativeForce() = this
            .map { it.cumulativeWorkers }
            .foldRight(0, Int::plus)
            .toDouble()

    private fun mildLoss(stratumPeoples: List<StratumPeople>) =
            loss(stratumPeoples, RandomSingleton.random.nextDouble() / 2)

    private fun bigLoss(stratumPeoples: List<StratumPeople>) =
            loss(stratumPeoples, RandomSingleton.random.nextDouble())

    private fun loss(stratumPeoples: List<StratumPeople>, part: Double) = stratumPeoples.forEach {
        it.decreaseAmount((it.workers * part).toInt())
    }

    override val internalToString = "A battle of ${firstSide.joinToString()} versus ${secondSide.joinToString()}"
}


enum class ConflictWinner { First, Second, Draw }

fun <E> ConflictWinner.decide(fst: E, snd: E, draw: E) = when (this) {
    First -> fst
    Second -> snd
    Draw -> draw
}
