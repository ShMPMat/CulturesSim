package simulation.space.resource

data class ResourceUpdateResult(val isAlive: Boolean, val produced: List<Resource> = emptyList())

val specialActions = mapOf(
        "_OnDeath_" to ResourceAction("_OnDeath_", listOf()),
        "_EachTurn_" to ResourceAction("_EachTurn_", listOf())
)

