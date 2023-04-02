package io.tashtabash.simulation.culture.group.request

import io.tashtabash.simulation.culture.aspect.Aspect
import io.tashtabash.simulation.space.resource.container.ResourcePack

data class ExecutedRequestResult(val pack: ResourcePack, val usedAspects: List<Aspect>)