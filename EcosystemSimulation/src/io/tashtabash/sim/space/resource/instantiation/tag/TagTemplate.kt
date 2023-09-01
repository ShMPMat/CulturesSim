package io.tashtabash.sim.space.resource.instantiation.tag

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.leveler.ResourceLeveler


class TagTemplate(val tag: ResourceTag, val valueTemplate: ResourceLeveler) {
    fun initialize(genome: Genome) = tag.copy(valueTemplate.getLevel(genome))
}
