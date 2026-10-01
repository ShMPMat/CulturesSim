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

    override fun hasNeeded(tile: Tile) = tile.resourcePack.any { isResourceGood(it) }

    fun isResourceGood(resource: Resource) = isResourceDependency(resource)

    open fun isResourceDependency(resource: Resource): Boolean =
        resource.isNotEmpty && oneResourceWorth(resource) != NOT_DEPENDENCY

    private val worthCache = ConcurrentHashMap<Genome, Int>()

    fun oneResourceWorth(resource: Resource): Int = worthCache.getOrPut(resource.genome) {
        if (labeler.isSuitable(resource.genome))
            labeler.actualMatches(resource.core.sample).sumOf { it.amount }
        else
            NOT_DEPENDENCY
    }

    fun partByResource(worth: Int, amount: Double) = ceil(amount / worth).toInt()

    // How far the resource can reach in one tick, in tiles
    protected fun accessRadius(tile: Tile, resource: Resource) =
        max(1.0, resource.genome.behaviour.tileSpeed(tile.scale)).toInt()

    override fun toString() = "$labeler of $amount"
}

const val NOT_DEPENDENCY = -1
