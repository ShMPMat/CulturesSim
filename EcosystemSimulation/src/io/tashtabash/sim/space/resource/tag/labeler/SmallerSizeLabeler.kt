package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome


data class SmallerSizeLabeler(private val size: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.sizeRange.first <= size

    override fun toString() = "Resource size is smaller or equals $size"
}
