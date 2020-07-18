package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.*
import simulation.culture.group.Add
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.MakeSplitGroupA
import simulation.culture.group.process.action.TryDivergeA
import simulation.culture.group.process.interaction.GroupTransferInteraction
import simulation.event.Event


object RandomGroupAddB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }

        if (options.isNotEmpty()) {
            val target = randomElement(options, session.random)
            return GroupTransferInteraction(group, target).run()
        }
        return emptyList()
    }

    override val internalToString = "Choose a random Neighbour and add it to the Conglomerate"
}

class TryDivergeB : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            if (TryDivergeA(group).run())
                listOf(Event(Event.Type.Change, "${group.name} diverged to it's own Conglomerate"))
            else listOf()

    override val internalToString = "Try to diverge and make Group's own Conglomerate"
}

class SplitGroupB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (!session.groupMultiplication)
            return emptyList()

        if (!group.populationCenter.isMaxReached(group.territoryCenter.territory))
            return emptyList()

        val tiles = group.overallTerritory.getOuterBrink {
            group.territoryCenter.canSettleAndNoGroup(it) && group.parentGroup.getClosestInnerGroupDistance(it) > 2
        }
        if (tiles.isEmpty())
            return emptyList()

        val tile = randomElement(tiles, session.random)
        val newGroup = MakeSplitGroupA(group, tile).run()

        Add(newGroup).execute(group.parentGroup)

        return listOf(Event(Event.Type.Creation, "${group.name} made a new group ${newGroup.name}"))
    }

    override val internalToString = "Try to split Group in two"
}
