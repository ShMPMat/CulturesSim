package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.tile.Tile


class LevelRestrictions(private val min: Int, private val max: Int) : ResourceDependency {
    override fun satisfactionPercent(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        val max = maxOf(0, tile.level - max, min - tile.level)

        return 1 / (1 + max).toDouble()
    }

    override val isNecessary = true

    override val isPositive = true

    override val isResourceNeeded = false

    override fun hasNeeded(tile: Tile) = tile.level in min..max
}
