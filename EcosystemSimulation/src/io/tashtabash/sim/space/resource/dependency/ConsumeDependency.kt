package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.tile.Tile
import java.util.*
import kotlin.math.ceil
import kotlin.math.min


class ConsumeDependency(
        deprivationCoefficient: Double,
        isNecessary: Boolean,
        labeler: QuantifiedResourceLabeler,
        var radius: Int = 1
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
        val oldAmount = currentAmount

        if (currentAmount < neededAmount)
            loop@for (list in tile.getAccessibleResources(radius))
                for (res in list) {
                    if (res == resource)
                        continue

                    if (isResourceDependency(res)) {
                        if (isSafe) {
                           currentAmount += res.amount * oneResourceWorth(res)
                        } else {
                            val expectedAmount = partByResource(res, neededAmount - currentAmount)
                            val part = res.getPart(
                                    expectedAmount,
                                    resource
                            )
                            if (part.isNotEmpty) {
                                lastConsumed(resource.baseName) += part.fullName
                                currentAmount += part.amount * oneResourceWorth(res)
                            }

                            part.destroy()
                        }

                        if (currentAmount >= neededAmount)
                            break@loop
                    }
                }

        result = min(currentAmount.toDouble() / neededAmount, 1.0)

        if (isSafe) {
            currentAmount = oldAmount
        }

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
