package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

interface ResourceTagLabeler {
    fun isSuitable(genome: Genome): Boolean
}