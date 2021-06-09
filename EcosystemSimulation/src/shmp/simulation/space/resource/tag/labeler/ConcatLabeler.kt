package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.Resource


data class ConcatLabeler(private val labelers: List<ResourceLabeler>) : ResourceLabeler {
    // It's actually faster than built-in "all {...}" (maybe)
    override fun isSuitable(genome: Genome): Boolean {
        for (labeler in labelers) {
            if (!labeler.isSuitable(genome))
                return false
        }
        return true
    }

    override fun toString() = labelers.joinToString(" and ", "(", ")")
}
