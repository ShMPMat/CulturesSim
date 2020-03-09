package simulation.space.resource.tag

import simulation.space.resource.ResourceCore

class TagLabeler(private val tag: ResourceTag) : ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.tags.contains(tag)
}