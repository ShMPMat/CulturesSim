package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.tag.ResourceTag

data class TagLabeler(private val tag: ResourceTag) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.tags.contains(tag)

    override fun toString() = "Resource has tag ${tag.name}"
}
