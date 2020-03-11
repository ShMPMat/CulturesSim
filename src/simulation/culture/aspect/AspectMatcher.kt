package simulation.culture.aspect

import simulation.space.resource.Resource
import simulation.space.resource.ResourceCore
import simulation.space.resource.ResourcePool
import simulation.space.resource.tag.labeler.ResourceTagLabeler

class AspectMatcher(
        private val labeler: ResourceTagLabeler,
        private val results: List<Pair<String, Int>>,
        private val coreName: String
) {
    init {
        if (results.isEmpty()) {
            throw ExceptionInInitializerError("Aspect matcher does nothing")
        }
    }

    fun match(core: ResourceCore): Boolean {
        return if (core.aspectConversion.keys
                        .map { obj: Aspect -> obj.name }
                        .any { name: String -> name == coreName }
        ) false
        else labeler.isSuitable(core)
    }

    fun getResults(resource: Resource, resourcePool: ResourcePool): List<Pair<Resource, Int>> {
        return results
                .map { (first, second) -> Pair(
                        if (first == "MATCHED") resource else resourcePool.get(first),
                        second
                ) }
    }
}