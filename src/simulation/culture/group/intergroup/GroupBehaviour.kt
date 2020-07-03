package simulation.culture.group.intergroup

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.session
import simulation.Event
import simulation.culture.group.centers.Group
import simulation.space.resource.Resource
import kotlin.math.pow

interface GroupBehaviour {
    fun run(group: Group): List<Event>

    fun update(group: Group): GroupBehaviour = this
}

sealed class AbstractGroupBehaviour : GroupBehaviour


object RandomTradeBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val groups = group.relationCenter.relatedGroups.sortedBy { it.name }

        if (groups.isEmpty()) return emptyList()

        val groupToTrade = randomElement(
                groups,
                { group.relationCenter.getNormalizedRelation(it).pow(2) },
                session.random
        )
        return TradeInteraction(group, groupToTrade, 1000).run()
    }
}


class MakeTradeResourceBehaviour(val amount: Int) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val resources = group.cultureCenter.aspectCenter.aspectPool.producedResources
                .map { it.first }

        if (resources.isEmpty()) return emptyList()

        val evaluator = { r: Resource -> group.cultureCenter.evaluateResource(r).toDouble().pow(3) }
        val chosenResource = randomElement(resources, evaluator, session.random)
        val pack = ProduceResourceA(group, chosenResource, amount, 20).run()

        val events = if (pack.isEmpty)
            emptyList()
        else
            listOf(Event(Event.Type.Creation, "${group.name} created resources for trade: $pack"))

        group.populationCenter.turnResources.addAll(pack)

        return events
    }
}


object RandomGroupAddBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }

        if (options.isNotEmpty()) {
            val target = randomElement(options, session.random)
            return GroupTransferInteraction(group, target).run()
        }
        return emptyList()
    }
}


class ChanceWrapperBehaviour(
        val behaviour: GroupBehaviour,
        val probability: Double,
        private val probabilityUpdate: (Group) -> Double = { probability }
) : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            if (testProbability(probability, session.random))
                behaviour.run(group)
            else emptyList()

    override fun update(group: Group) = ChanceWrapperBehaviour(
            behaviour.update(group),
            probabilityUpdate(group),
            probabilityUpdate
    )
}

fun GroupBehaviour.withProbability(probability: Double, probabilityUpdate: (Group) -> Double = { probability })
        = ChanceWrapperBehaviour(this, probability, probabilityUpdate)


class TimesWrapperBehaviour(
        val behaviour: GroupBehaviour,
        val min: Int,
        val max: Int = min + 1,
        private val minUpdate: (Group) -> Int = { min },
        private val maxUpdate: (Group) -> Int = { if (max != min + 1) max else minUpdate(it) + 1 }
) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val times = session.random.nextInt(min, max)
        return (0 until times).flatMap { behaviour.run(group) }
    }

    override fun update(group: Group) = TimesWrapperBehaviour(
            behaviour.update(group),
            minUpdate(group),
            maxUpdate(group),
            minUpdate,
            maxUpdate
    )
}

fun GroupBehaviour.times(
        min: Int,
        max: Int = min + 1,
        minUpdate: (Group) -> Int = { min },
        maxUpdate: (Group) -> Int = { if (max != min + 1) max else minUpdate(it) + 1 }
)
        = TimesWrapperBehaviour(this, min, max, minUpdate, maxUpdate)
