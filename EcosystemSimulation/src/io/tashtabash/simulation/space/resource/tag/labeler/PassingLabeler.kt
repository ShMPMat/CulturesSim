package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome


object PassingLabeler : ResourceLabeler {
    override fun isSuitable(genome: Genome) = true

    override fun toString() = "Any Resource"
}
