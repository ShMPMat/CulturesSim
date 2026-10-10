package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.dependency.NOT_DEPENDENCY
import java.util.concurrent.ConcurrentHashMap


abstract class ResourceLabeler {
    abstract fun isSuitable(genome: Genome): Boolean

    open fun actualMatches(resource: Resource): List<Resource> = if (isSuitable(resource.genome))
        listOf(resource)
    else
        emptyList()

    private val worthCache = ConcurrentHashMap<Genome, Int>()

    fun oneResourceWorth(resource: Resource): Int = worthCache.getOrPut(resource.genome) {
        if (isSuitable(resource.genome))
            actualMatches(resource.core.sample).sumOf { it.amount }
        else
            NOT_DEPENDENCY
    }
}
