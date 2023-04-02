package io.tashtabash.simulation.culture.group.process.behaviour

import io.tashtabash.simulation.culture.group.centers.AdministrationType
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.group.process.emptyProcessResult
import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.event.Type


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
