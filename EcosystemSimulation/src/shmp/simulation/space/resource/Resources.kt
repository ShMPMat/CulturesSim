package shmp.simulation.space.resource

import shmp.simulation.space.resource.action.ConversionCore
import shmp.simulation.space.resource.action.ResourceAction


data class ResourceUpdateResult(val isAlive: Boolean, val produced: List<Resource> = emptyList())


val specialActions = mapOf(
        "_OnDeath_" to ResourceAction("_OnDeath_", listOf(), listOf())
)


internal fun Resource.flingConversionLinks(old: Resource): Resource {
    val newConversionCore = ConversionCore(mapOf())

    genome.conversionCore.actionConversion.map { (action, results) ->
        action to results.map { (r, n) ->
            if (r == old)
                this to n
            else
                r to n
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
