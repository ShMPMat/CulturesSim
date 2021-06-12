package shmp.simulation.space.resource.action

import shmp.simulation.DataInitializationError
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.instantiation.ResourceStringTemplate
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler


class ActionMatcher(
        private val labeler: ResourceLabeler,
        private val results: List<Pair<String, Int>>,
        private val resourceActionName: String
) {
    init {
        if (results.isEmpty()) throw DataInitializationError("Action matcher does nothing")
    }

    fun constructResults(pool: ResourcePool) = results.flatMap { (n, a) ->
        pool.simpleNameMap.getOrDefault(n, listOf()).map { it.copy(a) }
    }

    fun match(resource: Resource) =
            if (!resource.genome.conversionCore.actionConversion.keys.map { it.name }.any { it == resourceActionName })
                if (resource.genome.hasLegacy && resource.simpleName in results.map { (n) -> n })
                    false
                else
                    labeler.isSuitable(resource.genome)
            else false

    fun getResults(resource: ResourceStringTemplate, resources: List<ResourceStringTemplate>): List<Pair<ResourceStringTemplate, Int>> =
            results.map { (name, amount) ->
                val resultResource =
                        if (name != "MATCHED")
                            resources.first { it.resource.baseName == name }
                        else resource
                resultResource to amount
            }
}
