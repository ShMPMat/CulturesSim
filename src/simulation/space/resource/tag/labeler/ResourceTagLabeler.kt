package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

interface ResourceTagLabeler {
    fun isSuitable(resource: Resource): Boolean
}