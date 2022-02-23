package shmp.simulation.space.resource.dependency

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.Taker
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import shmp.simulation.space.tile.Tile
import java.util.*
import kotlin.math.min


class ConsumeDependency(
        deprivationCoefficient: Double,
        isNecessary: Boolean,
        amount: Double,
        goodResource: ResourceLabeler,
        val radius: Int
) : LabelerDependency(deprivationCoefficient, isNecessary, amount, goodResource) {
    fun lastConsumed(name: String): MutableSet<String> = consumed.getOrPut(name) {
        HashSet<String>()
    }

    var currentAmount = 0

    override fun satisfaction(tile: Tile, resource: Resource): Double {
        if (currentAmount < 0)
            currentAmount = 0

        val result: Double
        val neededAmount = amount * resource.amount

        if (currentAmount < neededAmount)
            loop@for (list in tile.getAccessibleResources(radius))
                for (res in list) {
                    if (res == resource)
                        continue

                    if (isResourceDependency(res)) {
                        val part = res.getPart(
                                partByResource(res, neededAmount - currentAmount),
                                resource
                        )
                        if (part.isNotEmpty) {
                            lastConsumed(resource.baseName).add(part.fullName)
                            currentAmount += part.amount * oneResourceWorth(res)
                        }

                        part.destroy()
                        if (currentAmount >= neededAmount)
                            break@loop
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

private val consumed = mutableMapOf<String, MutableSet<String>>()
