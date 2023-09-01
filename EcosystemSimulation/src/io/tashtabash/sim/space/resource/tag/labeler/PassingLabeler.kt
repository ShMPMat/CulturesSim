package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome


object PassingLabeler : ResourceLabeler {
    override fun isSuitable(genome: Genome) = true

    override fun toString() = "Any Resource"
}
