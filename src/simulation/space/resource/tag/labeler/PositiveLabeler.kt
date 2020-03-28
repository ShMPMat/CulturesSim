package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

class PositiveLabeler : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = true
}