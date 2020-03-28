package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

data class ConcatLabeler(private val labelers: Collection<ResourceTagLabeler>) : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = labelers.all { it.isSuitable(resource) }
}