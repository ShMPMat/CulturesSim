package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class ConcatLabeler(private val labelers: Collection<ResourceLabeler>) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = labelers.all { it.isSuitable(genome) }
}