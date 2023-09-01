package io.tashtabash.sim.culture.group.place

import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.TileTag

class MovablePlace(tile: Tile, private val baseTag: TileTag) {
    var movedTimes = 0
        private set

    var current = StaticPlace(tile, tileTag)
        private set

    val tileTag: TileTag
     get() = TileTag(baseTag.name + movedTimes, baseTag.type)

    fun move(newTile: Tile) {
        if (newTile == current.tile) return
        val oldPlace = current
        movedTimes++
        current = StaticPlace(newTile, tileTag)
        val movableResources = oldPlace.getResourcesAndRemove { it.genome.isMovable }
        current.addResources(movableResources)
        session.world.strayPlacesManager.addPlace(oldPlace)
    }
}