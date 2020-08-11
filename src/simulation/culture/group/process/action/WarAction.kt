package simulation.culture.group.process.action

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.centers.Group
import kotlin.math.pow


class DecideWarDeclarationA(group: Group, val opponent: Group): AbstractGroupAction(group) {
    override fun run(): Boolean {
        val relation = group.relationCenter.getNormalizedRelation(opponent)
        val ownForcesEstimation = EstimateForcesA(group, group).run() + 1
        val opponentForcesEstimation = EstimateForcesA(group, opponent).run() + 1

        val relationCoefficient = 1 - relation.pow(5)
        val forcesCoefficient = ownForcesEstimation.pow(0.5) / opponentForcesEstimation.pow(0.5)

        return testProbability(relationCoefficient * forcesCoefficient, Controller.session.random)
    }

    override val internalToString = "Let ${group.name} decide, whether to declare a war to ${opponent.name}"
}
