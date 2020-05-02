package simulation.space.resource

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectCore
import simulation.culture.aspect.dependency.AspectDependencies


data class ResourceUpdateResult(val isAlive: Boolean, val produced: List<Resource> = emptyList())

val DEATH_ASPECT = Aspect(
        AspectCore(
                "_OnDeath_",
                emptyList(),
                emptyList(),
                emptyList(),
                applyMeaning = false,
                resourceExposed = false
        ),
        AspectDependencies(mutableMapOf())
)