package simulation.culture.group.process.action

import simulation.culture.group.Transfer
import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group


class ChangeRelationsA(
        group: Group,
        private val target: Group,
        private val delta: Double
) : AbstractGroupAction(group) {
    override fun run() {
        val targetRelation = group.relationCenter.relations.firstOrNull { it.other == target }
        if (targetRelation != null)
            targetRelation.positive += delta
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
