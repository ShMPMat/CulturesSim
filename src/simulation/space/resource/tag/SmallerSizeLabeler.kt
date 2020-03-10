package simulation.space.resource.tag

import simulation.space.resource.ResourceCore

class SmallerSizeLabeler(private val size: Double): ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.genome.size <= size
}