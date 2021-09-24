package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.Genome


data class SumLeveler(private val levelers: List<ResourceLeveler>) : ResourceLeveler {
    override fun getLevel(genome: Genome) = levelers.sumByDouble { it.getLevel(genome) }
}
