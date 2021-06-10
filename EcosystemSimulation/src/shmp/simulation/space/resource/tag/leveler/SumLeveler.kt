package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.tag.ResourceTag


data class SumLeveler(private val levelers: List<ResourceLeveler>) : ResourceLeveler {
    override fun getLevel(genome: Genome) = levelers.sumBy { it.getLevel(genome) }
}
