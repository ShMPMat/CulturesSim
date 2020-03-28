package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class SmallerSizeLabeler(private val size: Double): ResourceTagLabeler {
    override fun isSuitable(genome: Genome) = genome.size <= size
}