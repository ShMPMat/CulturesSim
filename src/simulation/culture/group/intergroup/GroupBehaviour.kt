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

class ChanceWrapperBehaviour(val behaviour: GroupBehaviour, val probability: Double) : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            if (testProbability(probability, session.random))
                behaviour.run(group)
            else emptyList()
}

fun GroupBehaviour.withProbability(probability: Double)
        = ChanceWrapperBehaviour(this, probability)

class TimesWrapperBehaviour(val behaviour: GroupBehaviour, val min: Int, val max: Int = min) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
         val times = session.random.nextInt(min, max)
        return (0 until times).flatMap { behaviour.run(group) }
    }
}

fun GroupBehaviour.times(min: Int, max: Int = min)
        = TimesWrapperBehaviour(this, min, max)
