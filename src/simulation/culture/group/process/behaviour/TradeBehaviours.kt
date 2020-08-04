package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.*
import simulation.event.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.CooperateA
import simulation.culture.group.process.action.MakeTradeResourcesA
import simulation.culture.group.process.action.ProduceExactResourceA
import simulation.culture.group.process.interaction.ChangeRelationsI
import simulation.culture.group.process.interaction.TradeI
import kotlin.math.pow


object RandomTradeB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val groups = group.relationCenter.relatedGroups.sortedBy { it.name }

        if (groups.isEmpty())
            return emptyList()

        val tradePartner = randomElement(
                groups,
                { group.relationCenter.getNormalizedRelation(it).pow(2) },
                session.random
        )
        return TradeI(group, tradePartner, 1000).run()
    }

    override val internalToString = "Trade with a random neighbour"
}


class MakeTradeResourceB(val amount: Int) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val pack = MakeTradeResourcesA(group, amount).run()

        val events = if (pack.isNotEmpty)
            listOf(Event(Event.Type.Creation, "${group.name} created resources for trade: $pack"))
        else emptyList()

        group.populationCenter.turnResources.addAll(pack)

        return events
    }

    override val internalToString = "Choose some valuable resource and try to create it"
}


class TradeRelationB(val partner: Group) : AbstractGroupBehaviour() {
    private var successes = 0.0
    private var fails = 0.0

    override fun run(group: Group): List<Event> {
        val result = TradeI(group, partner, 1000).run()

        if (result.none { it.type == Event.Type.Cooperation })
            fails++
        else
            successes++

        return result
    }

    override val internalToString = "Trade with ${partner.name}"

    override fun update(group: Group): TradeRelationB? {
        val continuationChance = (successes + 1.0) / (successes + fails + 1.0)

        return if (testProbability(continuationChance, session.random))
            this
        else null
    }
}


object EstablishTradeRelationsB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val groupsToChances = group.relationCenter.relations
                .map { it.other to it.normalized }
                .map { (g, p) -> g to p * g.populationCenter.stratumCenter.traderStratum.cumulativeWorkAblePopulation }
                .filter { (_, p) -> p > 0.0 }

        if (groupsToChances.isEmpty())
            return emptyList()

        val chosenPartner = randomElement(
                groupsToChances,
                { (_, p) -> p },
                session.random
        ).first

        if (!CooperateA(chosenPartner, group, 0.1).run())
            return ChangeRelationsI(group, chosenPartner, -1.0).run() +
                    listOf(Event(
                            Event.Type.Conflict,
                            "${group.name} tried to make a trade agreement with ${chosenPartner.name}, " +
                                    "but got rejected"
                    ))

        group.processCenter.addBehaviour(TradeRelationB(chosenPartner))

        return listOf(Event(
                Event.Type.Cooperation,
                "${group.name} made a trade agreement with a ${chosenPartner.name}"
        ))
    }

    override val internalToString = "Try to establish trade relations with a random Group"
}
