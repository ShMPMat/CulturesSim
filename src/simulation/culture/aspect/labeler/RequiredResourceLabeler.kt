package simulation.culture.aspect.labeler

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper
import simulation.space.resource.tag.labeler.ResourceLabeler

data class RequiredResourceLabeler(val labeler: ResourceLabeler) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect is ConverseWrapper && labeler.isSuitable(aspect.resource.genome)

    override fun toString(): String {
        return "Required resource - $labeler"
    }
}