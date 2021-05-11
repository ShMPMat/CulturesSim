package shmp.simulation.culture.aspect.labeler

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.ConverseWrapper


data class AspectNameLabeler(val aspectName: String) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect is ConverseWrapper && aspect.aspect.name == aspectName
            || aspect.name == aspectName

    override fun toString(): String {
        return "ConverseWrapper with Aspect $aspectName"
    }
}
