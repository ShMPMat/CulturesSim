package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome


data class SmallerDensityLabeler(private val density: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.primaryMaterial!!.density <= density //FIXME

    override fun toString() = "Resource density is smaller or equals $density"
}
