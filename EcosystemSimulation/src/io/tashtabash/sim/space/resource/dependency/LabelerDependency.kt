package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.tile.Tile
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.max


abstract class LabelerDependency(
    deprivationCoefficient: Double,
    override val isNecessary: Boolean,
    quantifiedResourceLabeler: QuantifiedResourceLabeler
) : CoefficientDependency(deprivationCoefficient) {
    override val isResourceNeeded = true

    val labeler = quantifiedResourceLabeler.resourceLabeler
    val amount = quantifiedResourceLabeler.amount

    override fun hasNeeded(tile: Tile) = tile.getDependencyMatches(this).any { res, _ -> res.isNotEmpty }

    open fun isResourceDependency(resource: Resource): Boolean =
        resource.isNotEmpty && oneResourceWorth(resource) != NOT_DEPENDENCY

    fun oneResourceWorth(resource: Resource): Int = labeler.oneResourceWorth(resource)

    fun partByResource(worth: Int, amount: Double) = ceil(amount / worth).toInt()

    // How far the resource can reach in one tick, in tiles
    protected fun accessRadius(tile: Tile, resource: Resource) =
        max(1.0, resource.genome.behaviour.tileSpeed(tile.scale)).toInt()

    override fun toString() = "$labeler of $amount"
}

const val NOT_DEPENDENCY = -1
