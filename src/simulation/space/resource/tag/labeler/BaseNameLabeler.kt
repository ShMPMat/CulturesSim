package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome
import simulation.space.resource.Resource

data class BaseNameLabeler(private val baseName: String) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.baseName == baseName

    override fun toString() = "Resource is $baseName"
}