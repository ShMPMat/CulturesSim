package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

class IsResistingLabeler : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = resource.genome.isResisting
}