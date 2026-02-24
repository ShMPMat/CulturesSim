package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult


object ResolveResourceNeedB : PlanBehaviour() {
    override fun run(group: Group): ProcessResult {
        val need = group.resourceCenter.mostImportantNeed
            ?: return emptyProcessResult

        (1 - 1.0 / (1 + need.second.importance / 20 )).chanceOfNot { // Development prob: 0 at 0, 0.5 at 20, 0.9 at 180
            return emptyProcessResult
        }

        group.cultureCenter.addNeedAspect(need.second)
        group.populationCenter.wakeNeedStrata(need)

        return emptyProcessResult
    }

    override val internalToString: String
        get() = "Check resource needs"
}
