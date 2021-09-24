package shmp.simulation.space.resource.instantiation.tag

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.leveler.TagLeveler


class TagTemplate(val tag: ResourceTag, val valueTemplate: TagLeveler) {
    fun initialize(genome: Genome) = tag.copy(valueTemplate.getLevel(genome))
}
