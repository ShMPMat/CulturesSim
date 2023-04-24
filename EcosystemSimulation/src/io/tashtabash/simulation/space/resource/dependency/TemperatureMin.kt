package io.tashtabash.simulation.space.resource.dependency

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.tile.Tile
import kotlin.math.max
import kotlin.math.sqrt


class TemperatureMin(threshold: Int, deprivationCoefficient: Double) : Temperature(threshold, deprivationCoefficient) {
    override fun satisfaction(tile: Tile, resource: Resource): Double {
        var result = (threshold - tile.temperature).toDouble()
        result = 1 / sqrt(max(0.0, result) + 1)
        return result
    }

    override fun hasNeeded(tile: Tile) = tile.temperature <= threshold
}
