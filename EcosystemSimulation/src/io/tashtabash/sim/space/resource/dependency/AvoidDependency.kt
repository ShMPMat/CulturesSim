package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.min


class AvoidDependency(
    deprivationCoefficient: Double,
    isNecessary: Boolean,
    labeler: QuantifiedResourceLabeler
) : LabelerDependency(deprivationCoefficient, isNecessary, labeler) {
    var lastConsumed = mutableSetOf<String>()

    override fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        val actualAmount = amount * resource.amount
        var currentAmount = 0

        tile.forEachAccessibleResource { res ->
            if (res.isEmpty || res == resource || !super.isResourceDependency(res))
                return@forEachAccessibleResource false

            currentAmount += res.amount * oneResourceWorth(res)

            currentAmount >= actualAmount
        }

        return 1 - min(currentAmount.toDouble() / actualAmount, 1.0)
    }

    override fun hasNeeded(tile: Tile) =
        tile.forEachAccessibleResource { isResourceDependency(it) }

    override fun isResourceDependency(resource: Resource) = false

    override val isPositive = false

    override fun toString() = "Avoid " + super.toString()
}
