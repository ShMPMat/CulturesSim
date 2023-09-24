package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.SpaceData.data
import kotlin.math.min


class Wind {
    var affectedTiles: MutableList<Pair<Tile, Double>> = mutableListOf()

    val isStill: Boolean
        get() = affectedTiles.isEmpty()

    val maxLevel: Double
        get() = affectedTiles
                .map { (_, t) -> t }
                .maxOrNull()
                ?: 0.0

    val sumLevel: Double
        get() = affectedTiles.sumOf { (_, t) -> t }

    fun changeLevelOnTile(tile: Tile, change: Double) {
        for (i in affectedTiles.indices) {
            var pair = affectedTiles[i]
            if (pair.first == tile) {
                pair = pair.first to min(change + pair.second, data.maximalWind)
                if (pair.second <= 0)
                    affectedTiles.removeAt(i)
                return
            }
        }
        if (change <= 0)
            return

        affectedTiles.add(Pair(tile, change))
    }

    fun getLevelByTile(tile: Tile): Double {
        affectedTiles
                .firstOrNull { (t) -> t == tile }
                ?.let { (_, t) -> return t }

        return affectedTiles
                .filter { (t) -> t == tile }
                .map { (_, t) -> t }
                .firstOrNull()
                ?: 0.0
    }
}
