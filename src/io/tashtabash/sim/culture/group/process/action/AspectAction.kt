package io.tashtabash.sim.culture.group.process.action

import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.ResourceNeed
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.AspectGaining
import io.tashtabash.sim.event.Fail
import io.tashtabash.sim.event.of


class AddRandomAspectA(group: Group, val options: List<Aspect>) : AbstractGroupAction(group) {
    override fun run(): ProcessResult {
        val aspectCenter = group.cultureCenter.aspectCenter

        options.randomElementOrNull()?.let { aspect ->
            if (aspect is ConverseWrapper && !aspectCenter.aspectPool.contains(aspect.aspect))
                return emptyProcessResult

            if (aspectCenter.tryAddingAspect(aspect, group))
                return ProcessResult(AspectGaining of "${group.name} developed aspect ${aspect.name} by itself")
        }

        return emptyProcessResult
    }

    override val internalToString = "Try adding Aspect"
}

class AddNeedAspectA(group: Group, val need: ResourceNeed) : AbstractGroupAction(group) {
    private val importanceToDepthCoefficient = 100

    override fun run(): ProcessResult {
        val aspectCenter = group.cultureCenter.aspectCenter
        val labeler = need.resourceLabeler
        val searchDepth = need.importance / importanceToDepthCoefficient + 1
        val option = aspectCenter.findRandomOption(need.resourceLabeler, group, searchDepth)

        if (option.isEmpty())
            return ProcessResult(Fail of "Group ${group.name} couldn't develop an aspect for a need $labeler")

        var success = true
        for ((aspect) in option.reversed()) {
            success = aspectCenter.tryAddingAspect(aspect, group)
            if (!success)
                break
        }
        val (aspect, sourceGroup) = option.first()

        return ProcessResult(
            if (success)
                if (sourceGroup == null)
                    AspectGaining of "Group ${group.name} developed an aspect ${aspect.name} for a need $labeler"
                else
                    AspectGaining of "Group ${group.name} took an aspect ${aspect.name} from ${sourceGroup.name}" +
                            " for a need $labeler"
            else
                Fail of "Group ${group.name} invented an aspect ${aspect.name} for a need $labeler but couldn't add it"
        )
    }

    override val internalToString = "Try developing an Aspect for a need"
}
