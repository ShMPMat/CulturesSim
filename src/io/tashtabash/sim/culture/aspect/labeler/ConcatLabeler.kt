package io.tashtabash.sim.culture.aspect.labeler

import io.tashtabash.sim.culture.aspect.Aspect

data class ConcatLabeler(private val labelers: Collection<AspectLabeler>) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = labelers.all { it.isSuitable(aspect) }

    override fun toString() = "All - " + labelers.joinToString()
}