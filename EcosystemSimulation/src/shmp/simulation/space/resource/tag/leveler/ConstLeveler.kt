package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.tag.ResourceTag


data class ConstLeveler(private val level: Double) : ResourceLeveler {
    override fun getLevel(genome: Genome) = level
}
