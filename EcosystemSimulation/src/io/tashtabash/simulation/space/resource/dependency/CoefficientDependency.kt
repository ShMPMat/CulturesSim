package io.tashtabash.simulation.space.resource.dependency

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.tile.Tile


abstract class CoefficientDependency(private val deprivationCoefficient: Double) : ResourceDependency {
    override fun satisfactionPercent(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        val result = satisfaction(tile, resource, isSafe)

        return result + (1 - result) / deprivationCoefficient
    }

    abstract fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double
}
