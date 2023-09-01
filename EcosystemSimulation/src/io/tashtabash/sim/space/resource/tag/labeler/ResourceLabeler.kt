package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.Resource


interface ResourceLabeler {
    fun isSuitable(genome: Genome): Boolean

    fun actualMatches(resource: Resource): List<Resource> = if (isSuitable(resource.genome))
        listOf(resource)
    else
        emptyList()
}
