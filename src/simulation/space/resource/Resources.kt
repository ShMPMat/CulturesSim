package simulation.space.resource

import simulation.space.resource.action.ResourceAction

data class ResourceUpdateResult(val isAlive: Boolean, val produced: List<Resource> = emptyList())

val specialActions = mapOf(
        "_OnDeath_" to ResourceAction("_OnDeath_", listOf())
)

