package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult


object UseCultureAspectsB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        for (aspect in group.cultureCenter.cultureAspectCenter.aspectPool.all)
            aspect.use(group)

        return ProcessResult()
    }

    override val internalToString = "Practice culture"
}
