package simulation.space.resource.action

import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePool
import simulation.space.resource.tag.labeler.ResourceLabeler

class ActionMatcher(
        private val labeler: ResourceLabeler,
        private val results: List<Pair<String, Int>>,
        private val resourceActionName: String
) {
    init {
        if (results.isEmpty()) throw ExceptionInInitializerError("Action matcher does nothing")
    }

    fun match(resource: Resource) =
            if (resource.conversionCore.actionConversion.keys.map { it.name }.any { it == resourceActionName })
                false
            else
                labeler.isSuitable(resource.genome)

    fun getResults(resource: Resource, resourcePool: ResourcePool): List<Pair<Resource, Int>> {
        return results
                .map { (name, amount) ->
                    val resultResource = if (name == "MATCHED") resource else resourcePool.get(name)
                    resultResource to amount
                }
    }
}
