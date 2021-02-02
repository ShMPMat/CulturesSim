package shmp.simulation.culture.group.process.behaviour

import shmp.simulation.culture.group.centers.AdministrationType
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.event.Event
import shmp.simulation.event.Type


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
