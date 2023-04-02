package io.tashtabash.simulation.space.resource.dependency

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.simulation.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.simulation.space.tile.Tile
import kotlin.math.ceil


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

    open fun isResourceDependency(resource: Resource): Boolean {
        if (resource.isEmpty)
            return false

        genomeHash[resource.genome]?.let {
            return it
        }

        val isDependency = labeler.isSuitable(resource.genome)
        genomeHash[resource.genome] = isDependency

        return isDependency
    }

    private val genomeHash = mutableMapOf<Genome, Boolean>()

    fun oneResourceWorth(resource: Resource) = labeler.actualMatches(resource.core.sample).sumBy(Resource::amount)

    fun partByResource(resource: Resource, amount: Double) = ceil(
            amount / labeler.actualMatches(resource.core.sample).sumBy(Resource::amount)
    ).toInt()

    override fun toString() = "$labeler of $amount"
}
