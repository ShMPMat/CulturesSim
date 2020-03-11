package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore

class ConcatLabeler(private val labelers: Collection<ResourceTagLabeler>) : ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = labelers.all { it.isSuitable(resourceCore) }
}