package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore

class IsResistingLabeler : ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.genome.isResisting
}