package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome


data class SmallerSizeLabeler(private val size: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.sizeRange.first <= size

    override fun toString() = "Resource size is smaller or equals $size"
}
