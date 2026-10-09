package io.tashtabash.sim

import io.tashtabash.sim.event.EventLog
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.generator.LandscapeData
import io.tashtabash.sim.space.resource.action.ActionMatcher
import io.tashtabash.sim.space.resource.action.ActionTag
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.container.ResourcePool
import io.tashtabash.sim.space.resource.instantiation.ResourceActionInjector
import io.tashtabash.sim.space.resource.instantiation.tag.TagParser
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.tile.LandscapeTile
import io.tashtabash.sim.space.tile.Tile


//Stores all entities in the simulation
interface World {
    var map: WorldMap
    var baseMap: LandscapeData
    val events: EventLog
    val tags: Set<ResourceTag>
    val resourcePool: ResourcePool
    val actionTags: List<ActionTag>
    val lesserTurnNumber: Int
    val turn: Int

    val year: Int
        get() = (turn / data.yearDurationTicks).toInt()

    val day: Int
        get() = ((turn % data.yearDurationTicks) * data.tickDurationDays).toInt() + 1

    fun initializeMap(
        actions: Map<ResourceAction, List<ActionMatcher>>,
        tagParser: TagParser,
        resourceActionInjectors: List<ResourceActionInjector>,
        proportionCoefficient: Double
    )

    fun stripBaseMap() {
        val landscape = map.lines.map { l ->
            l.map {
                LandscapeTile(
                    it.level.toShort(),
                    Tile.Type.entries.indexOf(it.type).toByte(),
                    it.plate?.id ?: 0
                )
            }
        }
        baseMap = LandscapeData(
            map.coveredRegion,
            map.scale,
            map.extent
        ) { x, y -> // Set id = 0 since the these Tiles will be thrown away immediately
            listOf(Tile(0, x, y, mutableListOf(), map.scale).also { landscape[x][y].applyToTile(it) })
        }
    }

    fun placeResources()
    fun incrementTurn()
    fun incrementTurnGeology()
}
