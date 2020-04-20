package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome
import simulation.space.resource.Resource

data class ConcatLabeler(private val labelers: Collection<ResourceLabeler>) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = labelers.all { it.isSuitable(genome) }

    override fun actualMatches(resource: Resource) = if (labelers.size == 1)
        labelers.first().actualMatches(resource)
    else
        super.actualMatches(resource)

    override fun toString() = labelers.joinToString(" and ", "(", ")")
}