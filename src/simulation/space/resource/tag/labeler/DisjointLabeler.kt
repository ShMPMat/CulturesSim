package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

open class DisjointLabeler(private val labelers: Collection<ResourceLabeler>) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = labelers.any { it.isSuitable(genome) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DisjointLabeler

        if (labelers != other.labelers) return false

        return true
    }

    override fun hashCode(): Int {
        return labelers.hashCode()
    }
}