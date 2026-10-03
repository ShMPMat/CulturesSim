package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.tile.Tile
import java.util.*


class AvoidTiles(badTypes: Collection<Tile.Type>) : ResourceDependency {
    val badTypes: Set<Tile.Type> = EnumSet.noneOf(Tile.Type::class.java).apply { addAll(badTypes) }

    override fun satisfactionPercent(tile: Tile, resource: Resource, isSafe: Boolean): Double =
        if (hasNeeded(tile)) 1.0
        else .0

    override val isNecessary = true

    override val isPositive = true

    override val isResourceNeeded = false

    override fun hasNeeded(tile: Tile) = !badTypes.contains(tile.type)

    override fun toString() = "Avoid " + badTypes.joinToString()
}
