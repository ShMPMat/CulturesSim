package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.TryDivergeA
import simulation.culture.group.process.interaction.GroupTransferInteraction
import simulation.event.Event


object RandomGroupAddB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }

        if (options.isNotEmpty()) {
            val target = randomElement(options, Controller.session.random)
            return GroupTransferInteraction(group, target).run()
        }
        return emptyList()
    }

    override val internalToString = "Choose a random Neighbour and add it to the Conglomerate"
}

class TryDivergeB : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            if (TryDivergeA(group).run())
                listOf(Event(Event.Type.Change, "${group.name} diverged to it's own Conglomerate"))
            else listOf()

    override val internalToString = "Try to diverge and make Group's own Conglomerate"
}
