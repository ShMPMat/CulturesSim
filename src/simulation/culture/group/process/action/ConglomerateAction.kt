package simulation.culture.group.process.action

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.Transfer
import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group
import java.util.*
import kotlin.math.pow

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

class TryDivergeA(group: Group) : AbstractGroupAction(group) {
    override fun run(): Boolean {
        if (!Controller.session.groupDiverge)
            return false
        if (group.parentGroup.subgroups.size <= 1)
            return false

        if (!checkCoherencyAndDiverge())
            NewConglomerateA(group, emptyList()).run()
        
        return true
    }

    private fun checkCoherencyAndDiverge(): Boolean {
        val queue: Queue<Group> = ArrayDeque()
        val cluster = mutableSetOf<Group>()
        queue.add(group)

        while (!queue.isEmpty()) {
            val cur = queue.poll()
            cluster.add(cur)
            queue.addAll(cur.territoryCenter.getAllNearGroups(cur)
                    .filter { it.parentGroup === group.parentGroup }
                    .filter { !cluster.contains(it) }
            )
        }

        if (group.parentGroup.subgroups.size == cluster.size)
            return false

        cluster.toList().let {
            NewConglomerateA(it[0], it.drop(1)).run()
        }

        return true
    }

    override val internalToString = "Try to diverge and make Group's own Conglomerate"
}
