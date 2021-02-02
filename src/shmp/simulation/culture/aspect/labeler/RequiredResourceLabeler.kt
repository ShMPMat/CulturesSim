package shmp.simulation.culture.aspect.labeler

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler

data class RequiredResourceLabeler(val labeler: ResourceLabeler) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect is ConverseWrapper && labeler.isSuitable(aspect.resource.genome)

    override fun toString(): String {
        return "Required resource - $labeler"
    }
}