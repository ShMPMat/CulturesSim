package io.tashtabash.simulation.culture.aspect.labeler

import io.tashtabash.simulation.culture.aspect.Aspect

interface AspectLabeler {
    fun isSuitable(aspect: Aspect): Boolean
}