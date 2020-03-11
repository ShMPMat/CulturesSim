package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore

interface ResourceTagLabeler {
    fun isSuitable(resourceCore: ResourceCore): Boolean
}