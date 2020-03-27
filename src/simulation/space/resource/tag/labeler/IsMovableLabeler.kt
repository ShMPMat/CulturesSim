package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

class IsMovableLabeler : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = resource.genome.isMovable
}