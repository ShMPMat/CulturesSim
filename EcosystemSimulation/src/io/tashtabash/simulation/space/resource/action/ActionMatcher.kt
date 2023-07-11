package io.tashtabash.simulation.space.resource.action

import io.tashtabash.simulation.DataInitializationError
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.container.ResourcePool
import io.tashtabash.simulation.space.resource.instantiation.ResourceStringTemplate
import io.tashtabash.simulation.space.resource.tag.labeler.ResourceLabeler


class ActionMatcher(
        private val labeler: ResourceLabeler,
        private val results: List<Pair<String, Int>>,
        private val resourceActionName: String
) {
    init {
        if (results.isEmpty())
            throw DataInitializationError("Action matcher does nothing")
    }

    fun constructResults(pool: ResourcePool) = results.flatMap { (n, a) ->
        pool.simpleNameMap
                .getOrDefault(n, listOf())
                .map { it.sample.copy(a) }
    }

    fun match(resource: Resource) =
            if (!resource.genome.conversionCore.actionConversions.keys.map { it.technicalName }.any { it == resourceActionName })
                if (resource.genome.hasLegacy && resource.simpleName in results.map { (n) -> n })
                    false
                else
                    labeler.isSuitable(resource.genome)
            else false

    fun getResults(resource: ResourceStringTemplate, resources: List<ResourceStringTemplate>): List<Pair<ResourceStringTemplate, Int>> =
            results.map { (name, amount) ->
                val resultResource =
                        if (name != "MATCHED")
                            resources.firstOrNull() { it.resource.baseName == name }
                                    ?: kotlin.run {
                                        resources[0]
                                    }
                        else resource
                resultResource to amount
            }
}
