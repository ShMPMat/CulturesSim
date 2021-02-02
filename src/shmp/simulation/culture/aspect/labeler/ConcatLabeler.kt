package shmp.simulation.culture.aspect.labeler

import shmp.simulation.culture.aspect.Aspect

data class ConcatLabeler(private val labelers: Collection<AspectLabeler>) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = labelers.all { it.isSuitable(aspect) }

    override fun toString() = "All - " + labelers.joinToString()
}