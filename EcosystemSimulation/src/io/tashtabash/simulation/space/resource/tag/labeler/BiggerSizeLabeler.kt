package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome


data class BiggerSizeLabeler(private val size: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.sizeRange.second >= size

    override fun toString() = "Size is bigger or equals $size"
}
