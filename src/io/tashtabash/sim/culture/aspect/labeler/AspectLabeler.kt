package io.tashtabash.sim.culture.aspect.labeler

import io.tashtabash.sim.culture.aspect.Aspect

interface AspectLabeler {
    fun isSuitable(aspect: Aspect): Boolean
}