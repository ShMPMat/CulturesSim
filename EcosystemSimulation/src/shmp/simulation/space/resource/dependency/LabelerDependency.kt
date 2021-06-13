package shmp.simulation.space.resource.dependency

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import shmp.simulation.space.tile.Tile
import kotlin.math.ceil


abstract class LabelerDependency(
        deprivationCoefficient: Double,
        override val isNecessary: Boolean,
        var amount: Double,
        private val labeler: ResourceLabeler
) : CoefficientDependency(deprivationCoefficient) {
    override val isResourceNeeded = true

    override fun hasNeeded(tile: Tile) = tile.resourcePack.any { isResourceGood(it) }

    fun isResourceGood(resource: Resource) = isResourceDependency(resource)

    open fun isResourceDependency(resource: Resource) =
            resource.isNotEmpty && labeler.isSuitable(resource.genome)

    fun oneResourceWorth(resource: Resource) = labeler.actualMatches(resource.core.sample).sumBy(Resource::amount)

    fun partByResource(resource: Resource, amount: Double) = ceil(
            amount / labeler.actualMatches(resource.core.sample).sumBy(Resource::amount)
    ).toInt()
}
