package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

data class SmallerDensityLabeler(private val density: Double): ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = resource.genome.primaryMaterial.density <= density
}