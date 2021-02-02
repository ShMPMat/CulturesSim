package shmp.simulation.culture.aspect.labeler

import shmp.simulation.culture.aspect.Aspect

interface AspectLabeler {
    fun isSuitable(aspect: Aspect): Boolean
}