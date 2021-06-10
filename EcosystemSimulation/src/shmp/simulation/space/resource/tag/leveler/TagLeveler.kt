package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.tag.ResourceTag


data class TagLeveler(private val tag: ResourceTag) : ResourceLeveler {
    override fun getLevel(genome: Genome) = genome.getTagLevel(tag)
}
