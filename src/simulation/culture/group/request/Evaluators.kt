package simulation.culture.group.request

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.ResourcePack

val zeroingEvaluator: ResourceEvaluator
    get() = ResourceEvaluator(
            { MutableResourcePack() },
            { 0 }
    )

val passingEvaluator: ResourceEvaluator
    get() = ResourceEvaluator(
            { it },
            ResourcePack::amount
    )
