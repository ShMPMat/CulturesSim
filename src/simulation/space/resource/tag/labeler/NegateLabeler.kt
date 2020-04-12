package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class NegateLabeler(val labeler: ResourceLabeler) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = !labeler.isSuitable(genome)
}