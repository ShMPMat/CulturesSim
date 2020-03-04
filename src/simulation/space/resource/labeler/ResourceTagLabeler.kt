package simulation.space.resource.labeler

import simulation.space.resource.Resource

interface ResourceTagLabeler {
    fun isSuitable(resource: Resource): Boolean
}