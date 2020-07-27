package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller.session
import simulation.culture.group.Add
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.MakeSplitGroupA
import simulation.culture.group.process.action.TryDivergeA
import simulation.culture.group.process.interaction.GroupTransferWithNegotiationI
import simulation.event.Event
import kotlin.math.pow
import kotlin.math.sqrt


object RandomGroupSeizureB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val groupValueMapper = { g: Group ->
            val relation = group.relationCenter.getNormalizedRelation(g).pow(3)
            val territoryValue = group.territoryCenter.territoryPotentialMapper(g.territoryCenter.territory).toDouble()
            relation * sqrt(territoryValue)
        }

        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }
                .map { it to groupValueMapper(it) }
                .filter { (_, n) -> n > 0 }

        if (options.isNotEmpty()) {
            val target = randomElement(options, { (_, n) -> n }, session.random).first
            return GroupTransferWithNegotiationI(group, target).run()
        }
        return emptyList()
    }

    override val internalToString = "Choose a random Neighbour and add it to the Conglomerate"
}

object TryDivergeB : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            if (TryDivergeA(group).run())
                listOf(Event(Event.Type.Change, "${group.name} diverged to it's own Conglomerate"))
            else listOf()

    override val internalToString = "Try to diverge and make Group's own Conglomerate"
}

object SplitGroupB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (!session.groupMultiplication)
            return emptyList()

        val tiles = group.overallTerritory.getOuterBrink {
            group.territoryCenter.canSettleAndNoGroup(it) && group.parentGroup.getClosestInnerGroupDistance(it) > 2
        }
        if (tiles.isEmpty())
            return emptyList()

        val tile = tiles.sortedBy { group.territoryCenter.tilePotentialMapper(it) }[0]
        val newGroup = MakeSplitGroupA(group, tile).run()

        Add(newGroup).execute(group.parentGroup)

        return listOf(Event(Event.Type.Creation, "${group.name} made a new group ${newGroup.name}"))
    }

    override val internalToString = "Try to split Group in two"
}
