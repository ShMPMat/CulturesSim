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
    fun lastConsumed(name: String): MutableMap<String, Int> = consumed.getOrPut(name) {
        HashMap()
    }

    var currentAmount = 0

    override fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        if (resource.amount == 0)
            return .0

        if (currentAmount < 0)
            currentAmount = 0

        val result: Double
        val neededAmount = amount * resource.amount
        val oldAmount = currentAmount
        val consumedAmounts = lastConsumed(resource.baseName)

        if (currentAmount < neededAmount)
            tile.forEachAccessibleResource(radius) { res ->
                if (res.isEmpty || res == resource || !isResourceDependency(res))
                    return@forEachAccessibleResource false

                if (isSafe)
                    currentAmount += res.amount * oneResourceWorth(res)
                else {
                    val expectedAmount = partByResource(res, neededAmount - currentAmount)
                    val part = res.getPartInt(expectedAmount, resource)
                    if (part != 0) {
                        consumedAmounts[res.fullName] = consumedAmounts.getOrDefault(res.fullName, 0) + part
                        currentAmount += part * oneResourceWorth(res)
                    }
                }

                return@forEachAccessibleResource currentAmount >= neededAmount
            }

        result = min(currentAmount.toDouble() / neededAmount, 1.0)

        if (isSafe)
            currentAmount = oldAmount

        if (currentAmount >= neededAmount)
            currentAmount -= ceil(neededAmount).toInt()

        return result
    }

    override val isPositive: Boolean
        get() = true

    override fun toString() = "Consume " + super.toString()
}

private val consumed = mutableMapOf<String, MutableMap<String, Int>>()

fun cleanConsumed() = consumed.forEach { it.value.clear() }
