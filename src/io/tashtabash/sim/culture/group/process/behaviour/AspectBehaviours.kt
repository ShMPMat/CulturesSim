package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.Change
import io.tashtabash.sim.event.of


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

object ForgetUnusedAspectsB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val crucialAspects = group.cultureCenter.cultureAspectCenter.aspectPool.cwDependencies
        val aspectPool = group.cultureCenter.aspectCenter.aspectPool
        val unimportantAspects = aspectPool.all
            .filter { it.usefulness < session.aspectFalloff }
            .filter { it !in crucialAspects }
        if (unimportantAspects.isEmpty())
            return ProcessResult()

        unimportantAspects.randomElementOrNull()
            ?.takeIf { aspect -> aspect !in aspectPool.converseWrappers.map { it.aspect } }
            ?.let { aspect ->
                if (aspect in group.cultureCenter.aspectCenter.changedAspectPool.converseWrappers.map { it.aspect })
                    return ProcessResult()

                if (group.cultureCenter.aspectCenter.remove(aspect))
                    ProcessResult(Change of "${group.name} lost aspect ${aspect.name}")
            }

        return ProcessResult()
    }

    override val internalToString = "Forget useless aspects"
}
