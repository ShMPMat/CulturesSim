package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

class IsResistingLabeler : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.isResisting

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString() = "Resource resists"
}
