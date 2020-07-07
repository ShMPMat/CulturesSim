package simulation.culture.group.process.action

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.Transfer
import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group
import simulation.culture.group.process.interaction.ChangeRelationsInteraction


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
}

class GrantHelpA(
        group: Group,
        private val target: Group,
        private val helpAmount: Double //range - 0-1
) : AbstractGroupAction(group) {
    override fun run(): Boolean {
        val probability = (1 - helpAmount) * group.relationCenter.getNormalizedRelation(group)
        val answer = testProbability(probability, Controller.session.random)

        val relationsChange = 1.0 * if (answer) 1 else -1
        ChangeRelationsInteraction(group, target, relationsChange).run()

        return answer
    }
}

class AddGroupA(group: Group, val groupToAdd: Group): AbstractGroupAction(group) {
    override fun run() {
        Transfer(groupToAdd).execute(group.parentGroup)
    }
}

class ProcessGroupRemovalA(group: Group, val groupToRemove: Group): AbstractGroupAction(group) {
    override fun run() {
        groupToRemove.processCenter.type = AdministrationType.Subordinate
    }
}
