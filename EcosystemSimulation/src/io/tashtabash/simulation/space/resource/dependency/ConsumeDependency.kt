package io.tashtabash.simulation.space.resource.dependency

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.simulation.space.tile.Tile
import java.util.*
import kotlin.math.ceil
import kotlin.math.min


class ConsumeDependency(
        deprivationCoefficient: Double,
        isNecessary: Boolean,
        labeler: QuantifiedResourceLabeler,
        val radius: Int
) : LabelerDependency(deprivationCoefficient, isNecessary, labeler) {
    fun lastConsumed(name: String): MutableSet<String> = consumed.getOrPut(name) {
        HashSet<String>()
    }

    var currentAmount = 0

    override fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        if (resource.amount == 0)
            return 0.0

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
                        if (isSafe) {
                           currentAmount += res.amount * oneResourceWorth(res)
                        } else {
                            val part = res.getPart(
                                    partByResource(res, neededAmount - currentAmount),
                                    resource
                            )
                            if (part.isNotEmpty) {
                                lastConsumed(resource.baseName).add(part.fullName)
                                currentAmount += part.amount * oneResourceWorth(res)
                            }

                            part.destroy()
                        }

                        if (currentAmount >= neededAmount)
                            break@loop
                    }
                }

        result = min(currentAmount.toDouble() / neededAmount, 1.0)

        if (currentAmount >= neededAmount)
            currentAmount -= ceil(neededAmount).toInt()

        return result
    }

    override val isPositive: Boolean
        get() = true

    override fun toString() = "Consume " + super.toString()
}

private val consumed = mutableMapOf<String, MutableSet<String>>()

fun cleanConsumed() = consumed.forEach { it.value.clear() }
