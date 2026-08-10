package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.min


class NeedDependency(
        deprivationCoefficient: Double,
        isNecessary: Boolean,
        labeler: QuantifiedResourceLabeler,
        var radius: Int = 1
) : LabelerDependency(deprivationCoefficient, isNecessary, labeler) {
    fun lastConsumed(name: String): MutableMap<String, Int> = needed.getOrPut(name) {
        HashMap()
    }

    override fun satisfaction(tile: Tile, resource: Resource, isSafe: Boolean): Double {
        val actualAmount = amount * resource.amount
        var currentAmount = 0

        loop@for (list in tile.getAccessibleResources(radius))
            for (res in list) {
                if (res == resource)
                    continue

                if (isResourceDependency(res)) {
                    currentAmount += res.amount * oneResourceWorth(res)

                    if (res.isNotEmpty && !isSafe) {
                        val neededAmounts = lastConsumed(resource.baseName)
                        val worth = res.amount * oneResourceWorth(res)
                        neededAmounts[res.fullName] = (neededAmounts[res.fullName] ?: 0) + worth
                    }

                    if (currentAmount >= actualAmount)
                        break@loop
                }
            }

        return min(currentAmount.toDouble() / actualAmount, 1.0)
    }

    override fun hasNeeded(tile: Tile) =
            tile.getAccessibleResources().any { it.asSequence().any { r -> isResourceDependency(r) } }

    override val isPositive = true

    override fun toString() = "Need " + super.toString()
}


private val needed = mutableMapOf<String, MutableMap<String, Int>>()

fun cleanNeeded() = needed.forEach { it.value.clear() }
