package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.space.resource.ResourcePack

data class RequestResult(val pack: ResourcePack, val usedAspects: List<Aspect>)