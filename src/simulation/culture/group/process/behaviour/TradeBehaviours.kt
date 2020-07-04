package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.ProduceResourceA
import simulation.culture.group.process.interaction.TradeInteraction
import simulation.space.resource.Resource
import kotlin.math.pow

object RandomTradeBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val groups = group.relationCenter.relatedGroups.sortedBy { it.name }

        if (groups.isEmpty()) return emptyList()

        val groupToTrade = randomElement(
                groups,
                { group.relationCenter.getNormalizedRelation(it).pow(2) },
                Controller.session.random
        )
        return TradeInteraction(group, groupToTrade, 1000).run()
    }

    override fun toString() = "Trade with a random neighbour"
}

class MakeTradeResourceBehaviour(val amount: Int) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val resources = group.cultureCenter.aspectCenter.aspectPool.producedResources
                .map { it.first }

        if (resources.isEmpty()) return emptyList()

        val evaluator = { r: Resource -> group.cultureCenter.evaluateResource(r).toDouble().pow(3) }
        val chosenResource = randomElement(resources, evaluator, Controller.session.random)
        val pack = ProduceResourceA(group, chosenResource, amount, 20).run()

        val events = if (pack.isEmpty)
            emptyList()
        else
            listOf(Event(Event.Type.Creation, "${group.name} created resources for trade: $pack"))

        group.populationCenter.turnResources.addAll(pack)

        return events
    }

    override fun toString() = "Choose some valuable resource and try to create it"
}
