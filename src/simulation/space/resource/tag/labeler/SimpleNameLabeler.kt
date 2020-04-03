package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome
import simulation.space.resource.material.Material

data class SimpleNameLabeler(private val simpleName: String) : ResourceTagLabeler {
    override fun isSuitable(genome: Genome) = genome.name == simpleName
}