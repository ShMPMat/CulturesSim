package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.sim.culture.group.centers.AdministrationType
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.Change
import io.tashtabash.sim.event.of


object ManageOwnType : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (group.parentGroup.subgroups.none { it.processCenter.type == AdministrationType.Main }) {
            group.processCenter.type = AdministrationType.Main
            return ProcessResult(Change of "${group.name} became a main Group in it's own Conglomerate")
        }

        return emptyProcessResult
    }

    override val internalToString = "Try to change own type"
}
