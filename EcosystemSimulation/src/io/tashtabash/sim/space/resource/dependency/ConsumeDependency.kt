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
    labeler: QuantifiedResourceLabeler
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
            tile.forEachAccessibleMatch(this, accessRadius(tile, resource)) { res, oneWorth ->
                if (res.isEmpty || res == resource)
                    return@forEachAccessibleMatch false

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

                return@forEachAccessibleMatch gatheredAmount >= neededAmount
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
