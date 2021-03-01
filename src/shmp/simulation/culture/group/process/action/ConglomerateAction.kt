package shmp.simulation.culture.group.process.action

import shmp.simulation.Controller
import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.Transfer
import shmp.simulation.culture.group.centers.*
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.thinking.meaning.GroupMemes
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.tile.Tile
import java.util.*


class AddGroupA(group: Group, private val newNeighbour: Group) : AbstractGroupAction(group) {
    override fun run() = Transfer(group).execute(newNeighbour.parentGroup)

    override val internalToString: String
        get() = "Add the group ${group.name} to the ${newNeighbour.parentGroup.name}"
}

class ProcessGroupRemovalA(group: Group, val groupToRemove: Group) : AbstractGroupAction(group) {
    override fun run() {
        groupToRemove.processCenter.type = AdministrationType.Subordinate
    }

    override val internalToString = "Let ${group.name} deal with the removal of ${groupToRemove.name}"
}

class GroupTransferA(group: Group, private val groupToAdd: Group) : AbstractGroupAction(group) {
    override fun run(): ProcessResult {
        AddGroupA(groupToAdd, group).run()
        ProcessGroupRemovalA(groupToAdd, groupToAdd).run()

        return ProcessResult(Event(
                Type.GroupInteraction,
                "${groupToAdd.name} joined to conglomerate ${group.parentGroup.name}"
        ))
    }

    override val internalToString = "Let ${groupToAdd.name} joined to conglomerate ${group.parentGroup.name}"
}


class NewConglomerateA(group: Group, val groups: List<Group>) : AbstractGroupAction(group) {
    override fun run(): GroupConglomerate {
        val conglomerate = GroupConglomerate(0, group.territoryCenter.center)

        Transfer(group).execute(conglomerate)
        for (newGroup in groups)
            AddGroupA(newGroup, group).run()

        Controller.session.world.addGroupConglomerate(conglomerate)

        return conglomerate
    }

    override val internalToString =
            "Creates a new Conglomerate with ${group.name} and additional ${groups.joinToString { it.name }}"
}

class TryDivergeA(group: Group) : AbstractGroupAction(group) {
    override fun run(): GroupConglomerate? {
        if (!Controller.session.groupDiverge)
            return null
        if (group.parentGroup.subgroups.size <= 1)
            return null

        return checkCoherencyAndDiverge()
                ?: NewConglomerateA(group, emptyList()).run()
    }

    private fun checkCoherencyAndDiverge(): GroupConglomerate? {
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
            return null

        return cluster.toList().let {
            NewConglomerateA(it[0], it.drop(1)).run()
        }
    }

    override val internalToString = "Try to diverge and make Group's own Conglomerate"
}

class MakeSplitGroupA(group: Group, private val startTile: Tile) : AbstractGroupAction(group) {
    override fun run(): Group {
        val aspects = group.cultureCenter.aspectCenter.aspectPool.all
                .map { it.copy(it.dependencies) }

        val memes = GroupMemes()
        memes.addAll(group.cultureCenter.memePool)

        val pack = MutableResourcePack()
        group.resourceCenter.pack.resources.forEach {
            pack.addAll(group.resourceCenter.takeResource(it, it.amount / 2))
        }

        val name = group.parentGroup.newName
        val memoryCenter = group.cultureCenter.memoryCenter.fullCopy()
        val aspectCenter = AspectCenter(aspects)

        return Group(
                ProcessCenter(AdministrationType.Subordinate),
                ResourceCenter(pack, startTile, name),
                group.parentGroup,
                name,
                group.populationCenter.getPart(0.5, startTile),
                RelationCenter(group.relationCenter.hostilityCalculator),
                CultureAspectCenter(
                        group.cultureCenter.cultureAspectCenter.reasonField.copy(),
                        baseConversions(memoryCenter, aspectCenter)
                ),
                group.cultureCenter.traitCenter.copy(),
                startTile,
                aspectCenter,
                memoryCenter,
                memes,
                group.cultureCenter.cultureAspectCenter.aspectPool.all,
                group.territoryCenter.spreadAbility
        )
    }

    override val internalToString = "Make a new Group from ${group.name}"
}

