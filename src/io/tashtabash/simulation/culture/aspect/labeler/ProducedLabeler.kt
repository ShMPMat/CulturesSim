package io.tashtabash.simulation.culture.aspect.labeler

import io.tashtabash.simulation.culture.aspect.Aspect
import io.tashtabash.simulation.space.resource.container.ResourcePool
import io.tashtabash.simulation.space.resource.tag.labeler.ResourceLabeler

data class ProducedLabeler(val labeler: ResourceLabeler) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect.producedResources.any { labeler.isSuitable(it.genome) }

    override fun toString(): String {
        return "Produced resource - $labeler"
    }
}


data class PotentialProducedLabeler(val labeler: ResourceLabeler, val pool: ResourcePool) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect.core.matchers.any { matcher ->
        matcher.constructResults(pool).any { labeler.isSuitable(it.genome) }
    }

    override fun toString(): String {
        return "Produced resource - $labeler"
    }
}
