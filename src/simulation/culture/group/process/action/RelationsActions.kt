package simulation.culture.group.process.action

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.centers.Trait
import simulation.culture.group.process.get
import simulation.culture.group.process.interaction.ChangeRelationsI


class ChangeRelationsA(
        group: Group,
        private val target: Group,
        private val delta: Double
) : AbstractGroupAction(group) {
    override fun run() {
        val targetRelation = group.relationCenter.relations.firstOrNull { it.other == target }
        if (targetRelation != null)
            targetRelation.positiveInteractions += delta
    }

    override val internalToString = "Change relations of ${group.name} and ${target.name} on $delta"
}

class CooperateA(
        group: Group,
        private val target: Group,
        private val helpAmount: Double //range - 0-1
) : AbstractGroupAction(group) {
    override fun run(): Boolean {
        val probability = (1 - helpAmount) * group.relationCenter.getNormalizedRelation(group)
        val answer = testProbability(probability, Controller.session.random) &&
                TestTraitA(group, Trait.Peace.get()).run()

        val relationsChange = 1.0 * if (answer) 1 else -1
        ChangeRelationsI(group, target, relationsChange).run()

        return answer
    }

    override val internalToString =
            "Let ${group.name} decide whether to cooperate with ${target.name}, help amount - $helpAmount"
}
