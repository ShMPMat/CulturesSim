package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.tag.ResourceTag


data class TagLabeler(private val tag: ResourceTag) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.getTagLevel(tag) > 0

    override fun toString() = "Resource has tag ${tag.name}"
}
