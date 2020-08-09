package simulation.space.resource.dependency

import simulation.space.resource.Resource
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile
import kotlin.math.min


class NeedDependency(
        amount: Double,
        deprivationCoefficient: Double,
        isNecessary: Boolean,
        goodResources: ResourceLabeler
) : LabelerDependency(deprivationCoefficient, isNecessary, amount, goodResources) {
    var lastConsumed = mutableSetOf<String>()

    override fun satisfaction(tile: Tile, resource: Resource): Double {
        val actualAmount = amount * resource.amount
        var currentAmount = 0

        loop@for (list in tile.accessibleResources)
            for (res in list) {
                if (res == resource)
                    continue

                if (isResourceDependency(res)) {
                    currentAmount += res.amount * oneResourceWorth(res)
                    if (currentAmount >= actualAmount)
                        break@loop
                }
            }

        return min(currentAmount.toDouble() / actualAmount, 1.0)
    }

    override fun hasNeeded(tile: Tile) =
            tile.accessibleResources.any { it.asSequence().any { r -> isResourceDependency(r) } }

    override val isPositive = true
}
