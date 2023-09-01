package io.tashtabash.sim.space.resource.tag.leveler

import io.tashtabash.sim.space.resource.Genome


data class ConstLeveler(private val level: Double) : ResourceLeveler {
    override fun getLevel(genome: Genome) = level
}
