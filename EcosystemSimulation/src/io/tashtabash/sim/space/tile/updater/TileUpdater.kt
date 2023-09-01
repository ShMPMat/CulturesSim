package io.tashtabash.sim.space.tile.updater

import io.tashtabash.sim.space.tile.Tile


interface TileUpdater {
    fun update(tile: Tile)
}
