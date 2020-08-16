package simulation.culture.group.process.behaviour

import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.emptyProcessResult
import simulation.event.Event
import simulation.event.Type


object ManageOwnType : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (group.parentGroup.subgroups.none { it.processCenter.type == AdministrationType.Main }) {
            group.processCenter.type = AdministrationType.Main
            return ProcessResult(Event(
                    Type.Change,
                    "${group.name} became a main Group in it's own Conglomerate"
            ))
        }

        return emptyProcessResult
    }

    override val internalToString = "Try to change own type"
}
