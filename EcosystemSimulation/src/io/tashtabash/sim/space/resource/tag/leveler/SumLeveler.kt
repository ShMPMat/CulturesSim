package io.tashtabash.sim.space.resource.tag.leveler

import io.tashtabash.sim.space.resource.Genome


data class SumLeveler(private val levelers: List<ResourceLeveler>) : ResourceLeveler {
    override fun getLevel(genome: Genome) = levelers.sumOf { it.getLevel(genome) }
}
