package io.tashtabash.sim.culture.group.request

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.space.resource.container.ResourcePack

data class ExecutedRequestResult(val pack: ResourcePack, val usedAspects: List<Aspect>)