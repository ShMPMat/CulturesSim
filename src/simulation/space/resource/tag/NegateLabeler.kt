package simulation.space.resource.tag

import simulation.space.resource.ResourceCore

class NegateLabeler(val labeler: ResourceTagLabeler) : ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = !labeler.isSuitable(resourceCore)
}