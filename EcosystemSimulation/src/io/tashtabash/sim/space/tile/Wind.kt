package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.SpaceData.data
import kotlin.math.min


class Wind {
    var affectedTiles: MutableList<Pair<Tile, Double>> = mutableListOf()

    val isStill: Boolean
        get() = affectedTiles.isEmpty()

    val maxLevel: Double
        get() = affectedTiles.maxOfOrNull { (_, t) -> t }
            ?: 0.0

    fun changeLevelOnTile(tile: Tile, change: Double) {
        for (i in affectedTiles.indices) {
            val (affectedTile, level) = affectedTiles[i]
            if (affectedTile == tile) {
                val newLevel = min(change + level, data.maxWind)
                if (newLevel <= 0)
                    affectedTiles.removeAt(i)
                else
                    affectedTiles[i] = tile to newLevel
                return
            }
        }
        if (change <= 0)
            return

        affectedTiles.add(Pair(tile, min(change, data.maxWind)))
    }

    fun getLevelByTile(tile: Tile): Double = affectedTiles
            .firstOrNull { (t) -> t == tile }
            ?.second
            ?: 0.0
}
