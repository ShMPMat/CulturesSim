package simulation.culture.aspect

import simulation.space.resource.tag.labeler.ResourceLabeler

data class AspectLabeler(val resourceLabeler: ResourceLabeler) {
    fun isSuitable(aspect: Aspect) = aspect.producedResources.any { resourceLabeler.isSuitable(it.genome) }
}