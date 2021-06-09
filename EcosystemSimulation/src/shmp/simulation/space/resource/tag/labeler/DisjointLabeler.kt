package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.Resource


open class DisjointLabeler(private val labelers: List<ResourceLabeler>) : ResourceLabeler {
    // It's actually faster than built-in "any {...}" (maybe)
    override fun isSuitable(genome: Genome): Boolean {
        for (labeler in labelers) {
            if (labeler.isSuitable(genome))
                return true
        }
        return false
    }

    override fun actualMatches(resource: Resource) = labelers
            .flatMap { it.actualMatches(resource) }

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

    override fun toString() = labelers.joinToString(" or ", "(", ")")
}
