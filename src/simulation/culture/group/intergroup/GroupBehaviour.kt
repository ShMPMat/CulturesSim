package simulation.culture.group.intergroup

import shmp.random.randomElement
import simulation.Controller.session
import simulation.culture.group.centers.Group
import kotlin.math.pow

interface GroupBehaviour {
    fun run()
}

sealed class AbstractGroupBehaviour(val group: Group): GroupBehaviour

class RandomTradeBehaviour(group: Group): AbstractGroupBehaviour(group) {
    override fun run() {
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