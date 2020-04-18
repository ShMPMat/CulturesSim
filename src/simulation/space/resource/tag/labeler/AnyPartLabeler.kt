package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class AnyPartLabeler(private val labeler: ResourceLabeler) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.parts.any { labeler.isSuitable(it.genome) }
}