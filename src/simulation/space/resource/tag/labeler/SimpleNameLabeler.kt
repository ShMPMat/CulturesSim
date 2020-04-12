package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class SimpleNameLabeler(private val simpleName: String) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.name == simpleName
}