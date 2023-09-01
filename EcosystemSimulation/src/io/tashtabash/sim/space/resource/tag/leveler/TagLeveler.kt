package io.tashtabash.sim.space.resource.tag.leveler

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.tag.ResourceTag


data class TagLeveler(private val tag: ResourceTag) : ResourceLeveler {
    override fun getLevel(genome: Genome) = genome.getTagLevel(tag)
}
