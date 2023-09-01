package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome


data class NegateLabeler(val labeler: ResourceLabeler) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = !labeler.isSuitable(genome)

    override fun toString() = "Not ($labeler)"
}
