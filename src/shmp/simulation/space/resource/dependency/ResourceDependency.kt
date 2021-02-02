package shmp.simulation.space.resource.dependency

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.tile.Tile

interface ResourceDependency {
    fun satisfactionPercent(tile: Tile, resource: Resource): Double
    val isNecessary: Boolean
    val isPositive: Boolean
    val isResourceNeeded: Boolean
    fun hasNeeded(tile: Tile): Boolean
}
