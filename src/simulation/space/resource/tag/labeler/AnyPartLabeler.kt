package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome
import simulation.space.resource.Resource

data class AnyPartLabeler(private val labeler: ResourceLabeler) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.parts.any { labeler.isSuitable(it.genome) }

    override fun actualMatches(resource: Resource) = resource.genome.parts.filter { labeler.isSuitable(it.genome) }

    override fun toString() = "Any Part - ($labeler)"
}
