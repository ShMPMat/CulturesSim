package simulation.space.resource.dependency

import simulation.space.resource.Resource
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile
import kotlin.math.ceil


abstract class LabelerDependency(
        deprivationCoefficient: Double,
        override val isNecessary: Boolean,
        var amount: Double,
        private val goodResource: ResourceLabeler
) : CoefficientDependency(deprivationCoefficient) {
    override val isResourceNeeded = true

    override fun hasNeeded(tile: Tile) = tile.resourcePack.any { isResourceGood(it) }

    fun isResourceGood(resource: Resource) = isResourceDependency(resource)

    open fun isResourceDependency(resource: Resource) =
            goodResource.isSuitable(resource.genome) && resource.isNotEmpty

    fun oneResourceWorth(resource: Resource) =
            goodResource.actualMatches(resource.copy(1, resource.ownershipMarker))
                    .map(Resource::amount)
                    .foldRight(0, Int::plus)

    fun partByResource(resource: Resource, amount: Double) = ceil(
            amount / goodResource.actualMatches(resource.copy(1, resource.ownershipMarker))
            .map(Resource::amount)
            .foldRight(0, Int::plus)
    ).toInt()
}
