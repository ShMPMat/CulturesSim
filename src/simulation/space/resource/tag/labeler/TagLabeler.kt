package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag

data class TagLabeler(private val tag: ResourceTag) : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = resource.tags.contains(tag)
}