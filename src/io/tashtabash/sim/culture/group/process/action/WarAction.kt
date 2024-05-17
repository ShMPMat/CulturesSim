package io.tashtabash.sim.culture.group.process.action

import io.tashtabash.random.singleton.testProbability
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.process.get
import kotlin.math.pow


class DecideWarDeclarationA(group: Group, val opponent: Group): AbstractGroupAction(group) {
    override fun run(): Boolean =
        EvaluateWarProbabilityA(group, opponent).run().testProbability()

    override val internalToString = "Let ${group.name} decide, whether to declare a war to ${opponent.name}"
}


class EvaluateWarProbabilityA(group: Group, val opponent: Group): AbstractGroupAction(group) {
    override fun run(): Double {
        val relation = group.relationCenter.getNormalizedRelation(opponent)
        val ownForcesEstimation = EstimateForcesA(group, group).run() + 1
        val opponentForcesEstimation = EstimateForcesA(group, opponent).run() + 1

        val relationCoefficient = 1 - relation.pow(3)
        val forcesCoefficient = ownForcesEstimation.pow(0.5) / opponentForcesEstimation.pow(0.5)
        val warlike = 1 - Trait.Peace.get().extract(group.cultureCenter.traitCenter)

        return warlike * relationCoefficient * forcesCoefficient
    }

    override val internalToString = "Let ${group.name} estimate the war probability with ${opponent.name}"
}
