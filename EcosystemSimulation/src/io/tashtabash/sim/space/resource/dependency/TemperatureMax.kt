package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.max
import kotlin.math.sqrt


class TemperatureMax(threshold: Int, deprivationCoefficient: Double) : Temperature(threshold, deprivationCoefficient) {
    override fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        var result = (tile.temperature - threshold).toDouble()
        result = 1 / sqrt(max(0.0, result) + 1)
        return result
    }

    override fun hasNeeded(tile: Tile) = tile.temperature >= threshold
}
