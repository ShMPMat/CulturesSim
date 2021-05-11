package shmp.simulation.culture.aspect.labeler

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler

data class ProducedLabeler(val labeler: ResourceLabeler) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect.producedResources.any { labeler.isSuitable(it.genome) }

    override fun toString(): String {
        return "Produced resource - $labeler"
    }
}


data class PotentialProducedLabeler(val labeler: ResourceLabeler, val pool: ResourcePool) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect.core.resourceAction.matchers.any { matcher ->
        matcher.constructResults(pool).any { labeler.isSuitable(it.genome) }
    }

    override fun toString(): String {
        return "Produced resource - $labeler"
    }
}
