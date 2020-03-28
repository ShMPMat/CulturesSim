package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

data class BiggerSizeLabeler(private val size: Double): ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = resource.genome.size >= size
}