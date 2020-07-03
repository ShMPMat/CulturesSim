package simulation.culture.group.intergroup

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.session
import simulation.Event
import simulation.culture.group.Transfer
import simulation.culture.group.centers.Group
import kotlin.math.pow

interface GroupBehaviour {
    fun run(group: Group): List<Event>

    fun update(group: Group): GroupBehaviour
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

    override fun update(group: Group) = this
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

    override fun update(group: Group) = this
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
        val max: Int = min,
        private val minUpdate: (Group) -> Int = { min },
        private val maxUpdate: (Group) -> Int = { max }
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
        max: Int = min,
        minUpdate: (Group) -> Int = { min },
        maxUpdate: (Group) -> Int = { max }
)
        = TimesWrapperBehaviour(this, min, max, minUpdate, maxUpdate)
