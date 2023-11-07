package io.tashtabash.sim.culture.group.process.action

import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.AspectGaining
import io.tashtabash.sim.event.of


class AddRandomAspectA(group: Group, val options: List<Aspect>): AbstractGroupAction(group) {
    override fun run(): ProcessResult {
        val aspectCenter = group.cultureCenter.aspectCenter

        options.randomElementOrNull()?.let { aspect ->
            if (aspect is ConverseWrapper && !aspectCenter.aspectPool.contains(aspect.aspect))
                return emptyProcessResult

            if (aspectCenter.addAspectTry(aspect, group))
                return ProcessResult(AspectGaining of "${group.name} developed aspect ${aspect.name} by itself")
        }

        return emptyProcessResult
    }

    override val internalToString = "Try adding Aspect"
}
