package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.Resource


data class AnyPartLabeler(private val labeler: ResourceLabeler) : ResourceLabeler {
    override fun isSuitable(genome: Genome): Boolean = genome.parts
            .any { labeler.isSuitable(it.genome) || isSuitable(it.genome) }

    override fun actualMatches(resource: Resource) = resource.genome.parts
            .filter { labeler.isSuitable(it.genome) || isSuitable(it.genome) }

    override fun toString() = "Any Part - ($labeler)"
}
