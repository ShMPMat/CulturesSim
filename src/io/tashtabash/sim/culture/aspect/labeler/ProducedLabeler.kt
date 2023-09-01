package io.tashtabash.sim.culture.aspect.labeler

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler


data class ProducedLabeler(val labeler: ResourceLabeler) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect.producedResources.any { labeler.isSuitable(it.genome) }

    override fun toString(): String {
        return "Produced resource - $labeler"
    }
}

// Commented out since some aspects rn can have no attached resource
//data class PotentialProducedLabeler(val labeler: ResourceLabeler, val pool: ResourcePool) : AspectLabeler {
//    override fun isSuitable(aspect: Aspect) = aspect.core.matchers.any { matcher ->
//        matcher.constructResults(pool).any { labeler.isSuitable(it.genome) }
//    }
//
//    override fun toString(): String {
//        return "Produced resource - $labeler"
//    }
//}
