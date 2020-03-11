package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore
import simulation.space.resource.tag.labeler.ResourceTagLabeler

class SmallerSizeLabeler(private val size: Double): ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.genome.size <= size
}