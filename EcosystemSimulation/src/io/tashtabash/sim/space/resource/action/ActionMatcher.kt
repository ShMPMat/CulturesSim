package io.tashtabash.sim.space.resource.action

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.space.resource.Resource
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
            if (resource.simpleName in resourceNames) // Don't match Resources which would turn into themselves
                false
            else
                labeler.isSuitable(resource.genome)
        else false

    fun getResults(resource: Resource, resources: List<Resource>): List<Pair<Resource, Int>> =
        results.map { (name, amount) ->
            val resultResource =
                if (name != "MATCHED")
                    resources.firstOrNull { it.baseName == name }
                        ?: run { resources[0] }
                else resource
            resultResource to amount
        }
}
