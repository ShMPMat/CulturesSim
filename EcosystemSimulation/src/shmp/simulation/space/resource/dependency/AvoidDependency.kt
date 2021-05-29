package shmp.simulation.space.resource.dependency

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import shmp.simulation.space.tile.Tile
import kotlin.math.min


class AvoidDependency(
        amount: Double,
        deprivationCoefficient: Double,
        isNecessary: Boolean,
        goodResources: ResourceLabeler
) : LabelerDependency(deprivationCoefficient, isNecessary, amount, goodResources) {
    var lastConsumed = mutableSetOf<String>()

    override fun satisfaction(tile: Tile, resource: Resource): Double {
        val result: Double
        val actualAmount = amount * resource.amount
        var currentAmount = 0

        loop@for (list in tile.getAccessibleResources())
            for (res in list) {
                if (res == resource)
                    continue

                if (super.isResourceDependency(res)) {
                    currentAmount += res.amount * oneResourceWorth(res)
                    if (currentAmount >= actualAmount)
                        break@loop
                }
            }
        result = min(currentAmount.toDouble() / actualAmount, 1.0)

        return 1 - result
    }

    override fun hasNeeded(tile: Tile) =
            tile.getAccessibleResources().any { it.asSequence().any { r -> isResourceDependency(r) } }

    override fun isResourceDependency(resource: Resource) = false

    override val isPositive = false
}
