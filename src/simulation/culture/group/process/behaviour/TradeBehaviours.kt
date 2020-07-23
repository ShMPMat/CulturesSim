package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.event.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.ProduceExactResourceA
import simulation.culture.group.process.interaction.TradeI
import kotlin.math.pow

object RandomTradeB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val groups = group.relationCenter.relatedGroups.sortedBy { it.name }

        if (groups.isEmpty())
            return emptyList()

        val groupToTrade = randomElement(
                groups,
                { group.relationCenter.getNormalizedRelation(it).pow(2) },
                Controller.session.random
        )
        return TradeI(group, groupToTrade, 1000).run()
    }

    override val internalToString = "Trade with a random neighbour"
}

class MakeTradeResourceB(val amount: Int) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val resources = group.cultureCenter.aspectCenter.aspectPool.producedResources

        if (resources.isEmpty())
            return emptyList()

        val chosenResource = randomElement(resources, Controller.session.random)
        val pack = ProduceExactResourceA(group, chosenResource, amount, 20).run()

        val events = if (pack.isEmpty)
            emptyList()
        else
            listOf(Event(Event.Type.Creation, "${group.name} created resources for trade: $pack"))

        group.populationCenter.turnResources.addAll(pack)

        return events
    }

    override val internalToString = "Choose some valuable resource and try to create it"
}
