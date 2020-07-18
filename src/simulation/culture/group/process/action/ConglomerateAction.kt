package simulation.culture.group.process.action

import simulation.Controller
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.Transfer
import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group

class AddGroupA(group: Group, val groupToAdd: Group) : AbstractGroupAction(group) {
    override fun run() {
        Transfer(groupToAdd).execute(group.parentGroup)
    }

    override val internalToString = "Add a group ${groupToAdd.name} to the ${group.parentGroup.name}"
}

class ProcessGroupRemovalA(group: Group, val groupToRemove: Group) : AbstractGroupAction(group) {
    override fun run() {
        groupToRemove.processCenter.type = AdministrationType.Subordinate
    }

    override val internalToString = "Let ${group.name} deal with the removal of ${groupToRemove.name}"
}

class NewConglomerateA(group: Group, val groups: List<Group>) : AbstractGroupAction(group) {
    override fun run() {
        val conglomerate = GroupConglomerate(0, group.territoryCenter.center)
        group.parentGroup.removeGroup(group)
        conglomerate.addGroup(group)

        for (newGroup in groups)
            AddGroupA(group, newGroup).run()

        Controller.session.world.addGroupConglomerate(conglomerate)
    }

    override val internalToString =
            "Creates a new Conglomerate with ${group.name} and additional ${groups.joinToString { it.name }}"
}
