package io.tashtabash.sim.space

import io.tashtabash.sim.space.generator.setTileNeighbours
import io.tashtabash.sim.space.tile.createTestTile


// A map with one Tile per cell
fun createTestMap(sizeX: Int, sizeY: Int, withNeighbours: Boolean = true): WorldMap {
    val tiles = (0 until sizeX).flatMap { x -> (0 until sizeY).map { y -> createTestTile(x, y) } }
    val map = WorldMap(tiles, sizeX, sizeY)

    if (withNeighbours)
        setTileNeighbours(map)

    return map
}
