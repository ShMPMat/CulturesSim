package simulation.culture.group.process.behaviour

import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group
import simulation.event.Event

class ManageOwnType : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (group.parentGroup.subgroups.none { it.processCenter.type == AdministrationType.Main }) {
            group.processCenter.type = AdministrationType.Main
            return listOf(Event(
                    Event.Type.Change,
                    "${group.name} became a main Group in it's own Conglomerate"
            ))
        }

        return emptyList()
    }

    override val internalToString = "Try to change own type"
}
