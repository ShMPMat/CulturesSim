package io.tashtabash.sim.space.resource.tag.leveler

import io.tashtabash.sim.space.resource.Genome


data class MulLeveler(private val levelers: List<ResourceLeveler>) : ResourceLeveler {
    override fun getLevel(genome: Genome) = levelers.map { it.getLevel(genome) }
            .fold(1.0, Double::times)
}
