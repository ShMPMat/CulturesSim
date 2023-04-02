package io.tashtabash.simulation.space.tile

import io.tashtabash.simulation.space.SpaceError
import kotlin.math.abs


fun getDistance(tile1: Tile, tile2: Tile): Int =
        abs(tile1.x - tile2.x) + abs(tile1.y - tile2.y)

fun isCloser(tile1: Tile, tile2: Tile, distance: Int) = getDistance(tile1, tile2) <= distance

fun getClosest(tile: Tile, tiles: Collection<Tile>): Pair<Tile, Int> {
    return tiles
            .map { it to getDistance(tile, it) }
            .minByOrNull { it.second }
            ?: throw SpaceError("Empty tiles collection")
}
