package io.tashtabash.sim.space.territory

import io.tashtabash.sim.space.tile.Tile


interface MutableTerritory: Territory {
    override var center: Tile?

    fun add(tile: Tile?)
    fun remove(tile: Tile?)

    fun addAll(territory: Territory) = addAll(territory.tiles)
    fun addAll(tiles: Collection<Tile>) = tiles.forEach { add(it) }

    fun removeAll(tiles: Collection<Tile?>) = tiles.forEach { remove(it) }

    fun removeIf(predicate: (Tile) -> Boolean) = removeAll(filter(predicate))
}
