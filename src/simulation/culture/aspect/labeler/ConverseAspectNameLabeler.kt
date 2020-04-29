package simulation.culture.aspect.labeler

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper

data class ConverseAspectNameLabeler(val aspectName: String) : AspectLabeler {
    override fun isSuitable(aspect: Aspect) = aspect is ConverseWrapper && aspect.aspect.name == aspectName
}