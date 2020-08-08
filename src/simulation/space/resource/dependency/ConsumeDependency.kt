package simulation.space.resource.dependency

import simulation.space.resource.Resource
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile
import java.util.*
import kotlin.math.min


class ConsumeDependency(
        deprivationCoefficient: Double,
        isNecessary: Boolean,
        amount: Double,
        goodResource: ResourceLabeler?) : LabelerDependency(deprivationCoefficient, isNecessary, amount, goodResource
) {
    var lastConsumed: MutableSet<String> = HashSet()

    var currentAmount = 0

    override fun satisfaction(tile: Tile, resource: Resource): Double {
        if (currentAmount < 0)
            currentAmount = 0

        val result: Double
        val neededAmount = amount * resource.amount

        if (currentAmount < neededAmount) {
            for (list in tile.accessibleResources)
                for (res in list) {
                    if (res == resource)
                        continue

                    if (isResourceDependency(res)) {
                        val part = res.getPart(partByResource(res, neededAmount - currentAmount))
                        if (part.isNotEmpty) {
                            lastConsumed.add(part.fullName)
                            currentAmount += part.amount * oneResourceWorth(res)
                        }

                        part.destroy()
                        if (currentAmount >= neededAmount)
                            break
                    }
                }
        }

        result = min(currentAmount.toDouble() / neededAmount, 1.0)

        if (currentAmount >= neededAmount)
            currentAmount -= neededAmount.toInt()

        return result
    }

    override val isPositive: Boolean
        get() = true
}
