package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.interaction.GroupTransferInteraction


object RandomGroupAddBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }

        if (options.isNotEmpty()) {
            val target = randomElement(options, Controller.session.random)
            return GroupTransferInteraction(group, target).run()
        }
        return emptyList()
    }

    override fun toString() = "Choose a random Neighbour and add it to the Conglomerate"
}
