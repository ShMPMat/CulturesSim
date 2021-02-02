package shmp.simulation.culture.group.request

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.space.resource.container.ResourcePack

data class ExecutedRequestResult(val pack: ResourcePack, val usedAspects: List<Aspect>)