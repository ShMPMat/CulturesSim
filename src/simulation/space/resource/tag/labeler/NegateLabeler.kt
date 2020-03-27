package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

class NegateLabeler(val labeler: ResourceTagLabeler) : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = !labeler.isSuitable(resource)
}