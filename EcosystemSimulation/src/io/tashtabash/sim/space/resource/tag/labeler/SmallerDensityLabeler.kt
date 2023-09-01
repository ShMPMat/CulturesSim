package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome


data class SmallerDensityLabeler(private val density: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.primaryMaterial!!.density <= density //FIXME

    override fun toString() = "Resource density is smaller or equals $density"
}
