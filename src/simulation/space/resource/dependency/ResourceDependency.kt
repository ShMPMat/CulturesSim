package simulation.space.resource.dependency

import simulation.space.resource.Resource
import simulation.space.tile.Tile

interface ResourceDependency {
    fun satisfactionPercent(tile: Tile, resource: Resource): Double
    val isNecessary: Boolean
    val isPositive: Boolean
    val isResourceNeeded: Boolean
    fun hasNeeded(tile: Tile): Boolean
}
