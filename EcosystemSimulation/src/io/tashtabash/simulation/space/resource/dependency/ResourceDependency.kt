package io.tashtabash.simulation.space.resource.dependency

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.tile.Tile


interface ResourceDependency {
    fun satisfactionPercent(tile: Tile, resource: Resource, isSafe: Boolean = false): Double
    val isNecessary: Boolean
    val isPositive: Boolean
    val isResourceNeeded: Boolean

    fun hasNeeded(tile: Tile): Boolean
}
