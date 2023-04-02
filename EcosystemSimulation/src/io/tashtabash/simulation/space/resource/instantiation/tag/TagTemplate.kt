package io.tashtabash.simulation.space.resource.instantiation.tag

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.tag.ResourceTag
import io.tashtabash.simulation.space.resource.tag.leveler.ResourceLeveler


class TagTemplate(val tag: ResourceTag, val valueTemplate: ResourceLeveler) {
    fun initialize(genome: Genome) = tag.copy(valueTemplate.getLevel(genome))
}
