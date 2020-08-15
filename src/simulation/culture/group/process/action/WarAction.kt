package simulation.culture.group.process.action

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.centers.Trait
import simulation.culture.group.process.get
import kotlin.math.pow


class DecideWarDeclarationA(group: Group, val opponent: Group): AbstractGroupAction(group) {
    override fun run(): Boolean {
        val relation = group.relationCenter.getNormalizedRelation(opponent)
        val ownForcesEstimation = EstimateForcesA(group, group).run() + 1
        val opponentForcesEstimation = EstimateForcesA(group, opponent).run() + 1

        val relationCoefficient = 1 - relation.pow(3)
        val forcesCoefficient = ownForcesEstimation.pow(0.5) / opponentForcesEstimation.pow(0.5)
        val warlike = 1 - Trait.Peace.get().extract(group.cultureCenter.traitCenter)

        return testProbability(warlike * relationCoefficient * forcesCoefficient, Controller.session.random)
    }

    override val internalToString = "Let ${group.name} decide, whether to declare a war to ${opponent.name}"
}
