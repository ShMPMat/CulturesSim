package simulation.space.resource

data class ResourceUpdateResult(val isAlive: Boolean, val produced: List<Resource> = emptyList())

val DEATH_ACTION = ResourceAction("_OnDeath_")
