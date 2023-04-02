package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome


data class NegateLabeler(val labeler: ResourceLabeler) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = !labeler.isSuitable(genome)

    override fun toString() = "Not ($labeler)"
}
