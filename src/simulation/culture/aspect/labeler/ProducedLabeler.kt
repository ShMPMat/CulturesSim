package simulation.culture.aspect.labeler

import simulation.culture.aspect.Aspect
import simulation.space.resource.tag.labeler.ResourceLabeler

data class ProducedLabeler(val labeler: ResourceLabeler) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect.producedResources.any { labeler.isSuitable(it.genome) }

    override fun toString(): String {
        return "Produced resource - $labeler"
    }
}