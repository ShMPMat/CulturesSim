package io.tashtabash.simulation.space.resource.tag.leveler

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.tag.ResourceTag


data class TagLeveler(private val tag: ResourceTag) : ResourceLeveler {
    override fun getLevel(genome: Genome) = genome.getTagLevel(tag)
}
