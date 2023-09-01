package io.tashtabash.sim.culture.aspect.labeler

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.ConverseWrapper


data class AspectNameLabeler(val aspectName: String) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect is ConverseWrapper && aspect.aspect.name == aspectName
            || aspect.name == aspectName

    override fun toString(): String {
        return "ConverseWrapper with Aspect $aspectName"
    }
}
