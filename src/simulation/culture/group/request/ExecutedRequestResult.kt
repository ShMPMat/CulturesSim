package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.space.resource.ResourcePack

data class ExecutedRequestResult(val pack: ResourcePack, val usedAspects: List<Aspect>)