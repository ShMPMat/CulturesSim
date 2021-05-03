package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.Resource


interface ResourceLabeler {
    fun isSuitable(genome: Genome): Boolean

    fun actualMatches(resource: Resource): List<Resource> = if (isSuitable(resource.genome))
        listOf(resource)
    else
        emptyList()
}
