package simulation.culture.group.intergroup

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.session
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

class ChanceWrapperBehaviour(val behaviour: GroupBehaviour, val probability: Double): AbstractGroupBehaviour() {
    override fun run(group: Group) {
        if (testProbability(probability, session.random))
            behaviour.run(group)
    }
}

fun GroupBehaviour.withProbability(probability: Double)
        = ChanceWrapperBehaviour(this, probability)
