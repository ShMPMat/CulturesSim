package simulation.culture.group.process.action.pseudo

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.process.action.pseudo.BattlePA.Winner.*
import simulation.culture.group.stratum.WorkerBunch
import kotlin.math.min
import kotlin.math.pow


class BattlePA(val firstSide: List<WorkerBunch>, val secondSide: List<WorkerBunch>) : GroupPseudoAction {
    override fun run(): Winner {
        val firstSideForces = firstSide.getCumulativeForce()
        val secondSideForces = secondSide.getCumulativeForce()
        val drawChance = min(
                firstSideForces.pow(2) / secondSideForces,
                secondSideForces.pow(2) / firstSideForces
        )

        return randomElement(
                listOf(First to firstSideForces, Second to secondSideForces, Draw to drawChance),
                { (_, n) -> n },
                Controller.session.random
        ).first
    }

    enum class Winner { First, Second, Draw }

    private fun List<WorkerBunch>.getCumulativeForce() = this
            .map { it.cumulativeWorkers }
            .foldRight(0, Int::plus)
            .toDouble()

    override val internalToString = "A battle of ${firstSide.joinToString()} versus ${secondSide.joinToString()}"
}
