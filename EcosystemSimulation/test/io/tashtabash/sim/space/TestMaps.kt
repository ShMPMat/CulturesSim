package io.tashtabash.sim.space

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.generator.setTileNeighbours
import io.tashtabash.sim.space.tile.createTestTile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue


// A map with one Tile per cell
fun createTestMap(
    sizeX: Int,
    sizeY: Int,
    withNeighbours: Boolean = true,
    scale: Scale = data.defaultScale
): WorldMap {
    val tiles = (0 until sizeX).flatMap { x -> (0 until sizeY).map { y -> createTestTile(x, y, scale) } }
    val map = WorldMap(tiles, mockWorldExtent(sizeX, sizeY))

    if (withNeighbours)
        setTileNeighbours(map)

    return map
}

// Neighbourhoods are symmetric with opposite directions, and only connect Tiles of the map
fun assertValidTopology(map: WorldMap) {
    for (tile in map.tiles)
        for (neighbour in tile.neighbours) {
            assertTrue(neighbour != tile) { "Tile ${tile.id} is its own neighbour" }
            assertTrue(neighbour in map.tiles) { "Tile ${tile.id} has neighbour ${neighbour.id} outside the map" }
            assertEquals(tile.directionOf(neighbour)?.opposite, neighbour.directionOf(tile)) {
                "Neighbourhood of Tiles ${tile.id} and ${neighbour.id} isn't symmetric"
            }
        }
}
