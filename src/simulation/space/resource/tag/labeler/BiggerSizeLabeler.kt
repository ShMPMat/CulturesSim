package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class BiggerSizeLabeler(private val size: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.size >= size
}