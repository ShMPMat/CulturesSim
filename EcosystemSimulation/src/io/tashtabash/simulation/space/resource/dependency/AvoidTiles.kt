package io.tashtabash.simulation.space.resource.dependency

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.tile.Tile


class AvoidTiles(var badTypes: Set<Tile.Type?>) : ResourceDependency {
    override fun satisfactionPercent(tile: Tile, resource: Resource, isSafe: Boolean): Double =
            if (hasNeeded(tile)) 1.0
            else 0.0

    override val isNecessary = true

    override val isPositive = true

    override val isResourceNeeded = false

    override fun hasNeeded(tile: Tile) = !badTypes.contains(tile.type)
}
