package io.tashtabash.simulation.space.resource.tag.leveler

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.tag.ResourceTag


data class ConstLeveler(private val level: Double) : ResourceLeveler {
    override fun getLevel(genome: Genome) = level
}
