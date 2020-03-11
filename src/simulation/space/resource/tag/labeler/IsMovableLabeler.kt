package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceCore

class IsMovableLabeler : ResourceTagLabeler {
    override fun isSuitable(resourceCore: ResourceCore) = resourceCore.genome.isMovable
}