package io.tashtabash.sim.space.tile

import io.tashtabash.sim.Controller.Companion.session
import io.tashtabash.sim.space.SpaceError
import kotlin.math.abs
import kotlin.math.min


// Distance can be shorter when we calculate it across the maxY line
fun getDistance(tile1: Tile, tile2: Tile): Int =
        abs(tile1.x - tile2.x) +
        min(
            abs(tile1.y - tile2.y),
            session.world.map.maxY + min(tile1.y - tile2.y, tile2.y - tile1.y)
        )

fun isCloser(tile1: Tile, tile2: Tile, distance: Int) = getDistance(tile1, tile2) <= distance

fun getClosest(tile: Tile, tiles: Collection<Tile>): Pair<Tile, Int> {
    return tiles
            .map { it to getDistance(tile, it) }
            .minByOrNull { it.second }
            ?: throw SpaceError("Empty tiles collection")
}
