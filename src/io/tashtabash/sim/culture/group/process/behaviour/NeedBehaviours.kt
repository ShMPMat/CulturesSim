package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult


object ResolveResourceNeedB : PlanBehaviour() {
    override fun run(group: Group): ProcessResult {
        val need = group.resourceCenter.direNeed
            ?: return emptyProcessResult

        group.cultureCenter.addNeedAspect(need.second)
        group.populationCenter.wakeNeedStrata(need)

        return emptyProcessResult
    }

    override val internalToString: String
        get() = "Check resource needs"
}
