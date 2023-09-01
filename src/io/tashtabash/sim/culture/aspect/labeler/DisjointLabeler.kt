package io.tashtabash.sim.culture.aspect.labeler

import io.tashtabash.sim.culture.aspect.Aspect

data class DisjointLabeler(private val labelers: Collection<AspectLabeler>) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = labelers.any { it.isSuitable(aspect) }

    override fun toString() = "Any - " + labelers.joinToString()
}