package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore
import simulation.space.resource.tag.ResourceTag

class TagLabeler(private val tag: ResourceTag) : ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.tags.contains(tag)
}