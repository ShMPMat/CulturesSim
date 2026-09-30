package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.tile.Tile
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.min


class ConsumeDependency(
    deprivationCoefficient: Double,
    isNecessary: Boolean,
    labeler: QuantifiedResourceLabeler,
    var radius: Int = 1
) : LabelerDependency(deprivationCoefficient, isNecessary, labeler) {
    fun lastConsumed(name: String): MutableMap<String, Int> = consumed.getOrPut(name) {
        ConcurrentHashMap()
    }

    override fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        if (resource.amount == 0)
            return .0

        val neededAmount = amount * resource.amount
        val consumedAmounts = lastConsumed(resource.baseName)
        var gatheredAmount = resource.getConsumeBuffer(this)

        if (gatheredAmount < neededAmount)
            tile.forEachAccessibleResource(radius) { res ->
                if (res.isEmpty)
                    return@forEachAccessibleResource false
                val oneWorth = oneResourceWorth(res)
                if (oneWorth == NOT_DEPENDENCY || res == resource)
                    return@forEachAccessibleResource false

                if (isSafe)
                    gatheredAmount += res.amount * oneWorth
                else {
                    val expectedAmount = partByResource(oneWorth, neededAmount - gatheredAmount)
                    val part = res.getPartInt(expectedAmount, resource)
                    if (part != 0) {
                        consumedAmounts.merge(res.fullName, part, Int::plus)
                        gatheredAmount += part * oneWorth
                    }
                }

                return@forEachAccessibleResource gatheredAmount >= neededAmount
            }

        val result = min(gatheredAmount.toDouble() / neededAmount, 1.0)

        if (!isSafe)
            resource.setConsumeBuffer(this, gatheredAmount - ceil(neededAmount).toInt())

        return result
    }

    override val isPositive: Boolean
        get() = true

    override fun toString() = "Consume " + super.toString()
}

// Concurrent since Tiles are updated in parallel
private val consumed = ConcurrentHashMap<String, MutableMap<String, Int>>()

fun cleanConsumed() = consumed.forEach { it.value.clear() }
