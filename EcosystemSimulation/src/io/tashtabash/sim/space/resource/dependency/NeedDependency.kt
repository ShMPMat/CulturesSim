package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.tile.Tile
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min


class NeedDependency(
    deprivationCoefficient: Double,
    isNecessary: Boolean,
    labeler: QuantifiedResourceLabeler
) : LabelerDependency(deprivationCoefficient, isNecessary, labeler) {
    fun lastConsumed(name: String): MutableMap<String, Int> = needed.getOrPut(name) {
        ConcurrentHashMap()
    }

    override fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        val neededAmount = amount * resource.amount
        var currentAmount = 0
        val neededAmounts = lastConsumed(resource.baseName)

        tile.forEachAccessibleMatch(this, accessRadius(tile, resource)) { res, oneWorth ->
            if (res.isEmpty || res == resource)
                return@forEachAccessibleMatch false

            val worth = res.amount * oneWorth
            currentAmount += worth

            if (!isSafe)
                neededAmounts.merge(res.fullName, worth, Int::plus)

            currentAmount >= neededAmount
        }

        return min(currentAmount.toDouble() / neededAmount, 1.0)
    }

    override fun hasNeeded(tile: Tile) =
        tile.forEachAccessibleMatch(this) { res, _ -> res.isNotEmpty }

    override val isPositive = true

    override fun toString() = "Need " + super.toString()
}


// Concurrent since Tiles are updated in parallel
private val needed = ConcurrentHashMap<String, MutableMap<String, Int>>()

fun cleanNeeded() = needed.forEach { it.value.clear() }
