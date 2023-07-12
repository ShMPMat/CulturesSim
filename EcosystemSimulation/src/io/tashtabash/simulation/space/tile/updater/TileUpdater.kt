package io.tashtabash.simulation.space.tile.updater

import io.tashtabash.simulation.space.tile.Tile


interface TileUpdater {
    fun update(tile: Tile)
}
