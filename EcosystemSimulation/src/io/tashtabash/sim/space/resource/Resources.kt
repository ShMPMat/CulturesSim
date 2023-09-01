package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.tile.Tile


typealias Resources = List<Resource>
typealias TiledResource = Pair<Tile, Resource>


data class ResourceUpdateResult(val isAlive: Boolean, val produced: List<TiledResource> = emptyList())


val specialActions = mapOf(
        "_OnDeath_" to ResourceAction("_OnDeath_", listOf(), listOf()),
        "TakeApart" to ResourceAction("TakeApart", listOf(), listOf()),
        "Killing" to ResourceAction("Killing", listOf(), listOf()),
)


internal fun Resource.replaceRecursiveLinks(old: Resource): Resource {
    val newConversionCore = ConversionCore(mapOf())

    genome.conversionCore.actionConversions.map { (action, results) ->
        action to results.map { r ->
            if (r == old) this.copy(r.amount)
            else r
        }
    }.forEach { (a, r) -> newConversionCore.addActionConversion(a, r) }

    genome.conversionCore = newConversionCore

    return this
}


enum class ResourceType {
    Plant, Animal, Mineral, Building, Artifact, None
}


enum class OverflowType {
    Ignore, Migrate, Cut
}
