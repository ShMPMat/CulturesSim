package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

interface ResourceLabeler {
    fun isSuitable(genome: Genome): Boolean
}