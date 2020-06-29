package simulation.culture.group.intergroup

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.session
import simulation.culture.group.Transfer
import simulation.culture.group.centers.Group
import kotlin.math.pow

interface GroupBehaviour {
    fun run(group: Group)
}

sealed class AbstractGroupBehaviour: GroupBehaviour

object RandomTradeBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group) {
        val groups = group.relationCenter.relatedGroups.sortedBy { it.name }

        if (groups.isEmpty()) return

        val groupToTrade = randomElement(
                groups,
                { group.relationCenter.getNormalizedRelation(it).pow(5) },
                session.random
        )
        TradeInteraction(group, groupToTrade, 1000).run()
    }
}

object RandomGroupAddBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group) {
        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }

        if (options.isNotEmpty()) {
            val target = randomElement(options, session.random)
            GroupTransferInteraction(group, target).run()
        }
    }
}

class ChanceWrapperBehaviour(val behaviour: GroupBehaviour, val probability: Double): AbstractGroupBehaviour() {
    override fun run(group: Group) {
        if (testProbability(probability, session.random))
            behaviour.run(group)
    }
}

fun GroupBehaviour.withProbability(probability: Double)
        = ChanceWrapperBehaviour(this, probability)
