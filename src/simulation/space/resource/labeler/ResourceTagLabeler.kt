package simulation.space.resource.labeler

import simulation.space.resource.ResourceIdeal

interface ResourceTagLabeler {
    fun isSuitable(resource: ResourceIdeal): Boolean
}