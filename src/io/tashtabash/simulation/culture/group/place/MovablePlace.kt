package io.tashtabash.simulation.culture.group.place

import io.tashtabash.simulation.CulturesController.Companion.session
import io.tashtabash.simulation.space.tile.Tile
import io.tashtabash.simulation.space.tile.TileTag

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