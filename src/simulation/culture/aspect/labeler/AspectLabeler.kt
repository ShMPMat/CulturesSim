package simulation.culture.aspect.labeler

import simulation.culture.aspect.Aspect
import simulation.space.resource.tag.labeler.ResourceLabeler

interface AspectLabeler {
    fun isSuitable(aspect: Aspect): Boolean
}