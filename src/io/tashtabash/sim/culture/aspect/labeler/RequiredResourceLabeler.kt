package io.tashtabash.sim.culture.aspect.labeler

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler

data class RequiredResourceLabeler(val labeler: ResourceLabeler) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect is ConverseWrapper && labeler.isSuitable(aspect.resource.genome)

    override fun toString(): String {
        return "Required resource - $labeler"
    }
}