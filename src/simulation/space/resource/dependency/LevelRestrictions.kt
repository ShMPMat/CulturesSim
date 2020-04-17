package simulation.space.resource.dependency

import simulation.space.SpaceError
import simulation.space.resource.Resource
import simulation.space.tile.Tile

class LevelRestrictions(private val min: Int, private val max: Int) : ResourceDependency {
    override fun satisfactionPercent(tile: Tile, resource: Resource?): Double {
        val max = listOf(0, tile.level - max, min - tile.level).max() ?: throw SpaceError("Impossible error")
        return 1 / (1 + max).toDouble()
    }

    override fun isNecessary() = true

    override fun isPositive() = true

    override fun isResourceNeeded() = false

    override fun hasNeeded(tile: Tile) = tile.level in min..max
}