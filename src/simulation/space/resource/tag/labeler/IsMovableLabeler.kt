package simulation.space.resource.tag.labeler

import simulation.space.resource.Resource

class IsMovableLabeler : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = resource.genome.isMovable

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}