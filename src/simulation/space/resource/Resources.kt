package simulation.space.resource

data class ResourceUpdateResult(val isAlive: Boolean, val produced: List<Resource> = emptyList())

val DEATH_ACTION = ResourceAction("_OnDeath_", listOf())

val EACH_TURN_ACTION = ResourceAction("_EachTurn_", listOf())
