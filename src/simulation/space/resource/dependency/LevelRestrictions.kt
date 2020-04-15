package simulation.space.resource.dependency

import simulation.space.resource.Resource
import simulation.space.tile.Tile
import java.lang.RuntimeException

class LevelRestrictions(private val min: Int, private val max: Int) : ResourceDependency {
    override fun satisfactionPercent(tile: Tile, resource: Resource?): Double {
        val max = listOf(0, tile.level - max, min - tile.level).max() ?: throw RuntimeException()//TODO proper excepiton
        return 1 / (1 + max).toDouble()
    }

    override fun isNecessary(): Boolean {
        return true
    }

    override fun isPositive(): Boolean {
        return true
    }

    override fun isResourceNeeded(): Boolean {
        return false
    }

    override fun hasNeeded(tile: Tile): Boolean {
        return tile.level in min..max
    }

}