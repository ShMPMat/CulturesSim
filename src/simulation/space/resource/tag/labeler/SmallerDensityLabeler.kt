package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore

class SmallerDensityLabeler(private val density: Double): ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.genome.primaryMaterial.density <= density
}