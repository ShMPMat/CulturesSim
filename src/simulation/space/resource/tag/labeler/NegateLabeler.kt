package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class NegateLabeler(val labeler: ResourceTagLabeler) : ResourceTagLabeler {
    override fun isSuitable(genome: Genome) = !labeler.isSuitable(genome)
}