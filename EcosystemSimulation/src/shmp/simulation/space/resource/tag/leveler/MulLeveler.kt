package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.Genome


data class MulLeveler(private val levelers: List<ResourceLeveler>) : ResourceLeveler {
    override fun getLevel(genome: Genome) = levelers.map { it.getLevel(genome) }
            .fold(1.0, Double::times)
}
