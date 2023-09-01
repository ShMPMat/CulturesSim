package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome


data class BiggerSizeLabeler(private val size: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.sizeRange.second >= size

    override fun toString() = "Size is bigger or equals $size"
}
