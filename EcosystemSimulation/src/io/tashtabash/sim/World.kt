package io.tashtabash.sim

import io.tashtabash.sim.event.EventLog
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.resource.action.ActionMatcher
import io.tashtabash.sim.space.resource.action.ActionTag
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.container.ResourcePool
import io.tashtabash.sim.space.resource.instantiation.ResourceActionInjector
import io.tashtabash.sim.space.resource.instantiation.tag.TagParser
import io.tashtabash.sim.space.resource.tag.ResourceTag


//Stores all entities in the simulation
interface World {
    var map: WorldMap
    val events: EventLog
    val tags: Set<ResourceTag>
    val resourcePool: ResourcePool
    val actionTags: List<ActionTag>
    val lesserTurnNumber: Int
    fun getTurn(): Int

    fun initializeMap(
        actions: Map<ResourceAction, List<ActionMatcher>>,
        tagParser: TagParser,
        resourceActionInjectors: List<ResourceActionInjector>,
        proportionCoefficient: Double
    )

    fun placeResources()
    fun incrementTurn()
    fun incrementTurnGeology()
    fun getStringTurn(): String
}
