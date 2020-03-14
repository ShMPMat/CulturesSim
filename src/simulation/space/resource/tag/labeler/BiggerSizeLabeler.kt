package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore

class BiggerSizeLabeler(private val size: Double): ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.genome.size >= size
}