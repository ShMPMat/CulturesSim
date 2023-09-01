package io.tashtabash.sim.space.resource.action

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.instantiation.ResourceStringTemplate
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler


class ActionMatcher(
        private val labeler: ResourceLabeler,
        private val results: List<Pair<String, Int>>,
        private val resourceActionName: String
) {
    init {
        if (results.isEmpty())
            throw DataInitializationError("Action matcher does nothing")
    }

    private val resourceNames = results.map { (n) -> n }

    fun match(resource: Resource) =
            if (!resource.genome.conversionCore.actionConversions.keys.map { it.technicalName }.any { it == resourceActionName })
                if (resource.genome.hasLegacy && resource.simpleName in resourceNames)
                    false
                else
                    labeler.isSuitable(resource.genome)
            else false

    fun getResults(resource: ResourceStringTemplate, resources: List<ResourceStringTemplate>): List<Pair<ResourceStringTemplate, Int>> =
            results.map { (name, amount) ->
                val resultResource =
                        if (name != "MATCHED")
                            resources.firstOrNull { it.resource.baseName == name }
                                    ?: run { resources[0] }
                        else resource
                resultResource to amount
            }
}
