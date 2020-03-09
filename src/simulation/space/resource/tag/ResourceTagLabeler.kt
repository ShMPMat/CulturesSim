package simulation.space.resource.tag

import simulation.space.resource.ResourceCore

interface ResourceTagLabeler {
    fun isSuitable(resourceCore: ResourceCore): Boolean
}