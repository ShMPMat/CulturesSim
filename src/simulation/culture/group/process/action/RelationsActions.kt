package simulation.culture.group.process.action

import simulation.culture.group.Transfer
import simulation.culture.group.centers.Group


class ImproveRelationsA(group: Group, val target: Group, val amount: Double) : AbstractGroupAction(group) {
    override fun run() {
        val targetRelation = group.relationCenter.relations.firstOrNull { it.other == target }
        if (targetRelation != null)
            targetRelation.positive += amount
    }
}

class AddGroupA(group: Group, val groupToAdd: Group): AbstractGroupAction(group) {
    override fun run() {
        Transfer(groupToAdd).execute(group.parentGroup)
    }
}

class ProcessGroupRemovalA(group: Group, val groupToRemove: Group): AbstractGroupAction(group) {
    override fun run() {}
}
