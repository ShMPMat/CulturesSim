package simulation.culture.group.process.behaviour

import simulation.culture.group.ConflictResultEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.pseudo.ActionSequencePA
import simulation.culture.group.process.action.pseudo.EventfulGroupPseudoAction
import simulation.culture.group.process.interaction.BattleI
import simulation.event.Event


class WarB(
        val opponent: Group,
        private val initiatorWinAction: EventfulGroupPseudoAction,
        private val participatorWinAction: EventfulGroupPseudoAction,
        private val drawWinAction: EventfulGroupPseudoAction = ActionSequencePA()
): AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val results = BattleI(group, opponent).run()
        val battleResults = results.map { it.status }

        TODO()

        return results
    }

    override val internalToString = "Carry on war with ${opponent.name}"
}
