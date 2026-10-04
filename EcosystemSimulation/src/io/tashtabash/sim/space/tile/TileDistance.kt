package io.tashtabash.sim.space.tile

import io.tashtabash.sim.Controller.Companion.session
import io.tashtabash.sim.space.SpaceError


fun getDistance(tile1: Tile, tile2: Tile): Int = session.world.map.distance(tile1, tile2)

fun getClosest(tile: Tile, tiles: Collection<Tile>): Pair<Tile, Int> =
    tiles.map { it to getDistance(tile, it) }
        .minByOrNull { it.second }
        ?: throw SpaceError("Empty tiles collection")
