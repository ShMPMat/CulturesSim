package shmp.simulation.culture.group.process.behaviour

import shmp.random.randomElementOrNull
import shmp.random.randomUnwrappedElementOrNull
import shmp.random.testProbability
import shmp.random.toSampleSpaceObject
import shmp.simulation.Controller.session
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.action.CooperateA
import shmp.simulation.culture.group.process.action.MakeTradeResourcesA
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.culture.group.process.interaction.ChangeRelationsI
import shmp.simulation.culture.group.process.interaction.TradeI
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import kotlin.math.pow


object RandomTradeB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val groups = group.relationCenter.relatedGroups.sortedBy { it.name }

        val tradePartner = randomElementOrNull(
                groups,
                { group.relationCenter.getNormalizedRelation(it).pow(2) },
                session.random
        ) ?: return emptyProcessResult

        return TradeI(group, tradePartner, 1000).run()
    }

    override val internalToString = "Trade with a random neighbour"
}


class MakeTradeResourceB(val amount: Int) : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val pack = MakeTradeResourcesA(group, amount).run()

        val events = if (pack.isNotEmpty)
            ProcessResult(Event(Type.Creation, "${group.name} created resources for trade: $pack"))
        else emptyProcessResult

        group.populationCenter.turnResources.addAll(pack)

        return events
    }

    override val internalToString = "Choose some valuable resource and try to create it"
}


class TradeRelationB(val partner: Group) : AbstractGroupBehaviour() {
    private var successes = 0.0
    private var fails = 0.0

    override fun run(group: Group): ProcessResult {
        val result = TradeI(group, partner, 1000).run()

        if (result.events.none { it.type == Type.Cooperation })
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
    override fun run(group: Group): ProcessResult {
        val groupsToChances = group.relationCenter.relations
                .map {
                    it.other.toSampleSpaceObject(
                            it.normalized *
                                    it.other.populationCenter.stratumCenter.traderStratum.cumulativeWorkAblePopulation
                    )
                }
                .filter { it.probability > 0.0 }


        val chosenPartner = randomUnwrappedElementOrNull(
                groupsToChances,
                session.random
        ) ?: return emptyProcessResult

        if (!CooperateA(chosenPartner, group, 0.1).run())
            return ChangeRelationsI(group, chosenPartner, -1.0).run() +
                    ProcessResult(Event(
                            Type.Conflict,
                            "${group.name} tried to make a trade agreement with ${chosenPartner.name}, " +
                                    "but got rejected"
                    ))

        group.processCenter.addBehaviour(TradeRelationB(chosenPartner))

        return ProcessResult(Event(
                Type.Cooperation,
                "${group.name} made a trade agreement with a ${chosenPartner.name}"
        ))
    }

    override val internalToString = "Try to establish trade relations with a random Group"
}
