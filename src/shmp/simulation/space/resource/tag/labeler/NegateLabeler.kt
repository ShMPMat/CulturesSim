package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome

data class NegateLabeler(val labeler: ResourceLabeler) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = !labeler.isSuitable(genome)

    override fun toString() = "Not ($labeler)"
}
