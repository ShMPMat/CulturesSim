package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome

data class BiggerSizeLabeler(private val size: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.size >= size

    override fun toString() = "Size is bigger or equals $size"
}
