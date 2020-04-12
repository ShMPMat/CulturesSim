package simulation.culture.aspect

import simulation.space.resource.Resource
import simulation.space.resource.ResourcePool
import simulation.space.resource.tag.labeler.ResourceLabeler

class AspectMatcher(
        private val labeler: ResourceLabeler,
        private val results: List<Pair<String, Int>>,
        private val coreName: String
) {
    init {
        if (results.isEmpty()) throw ExceptionInInitializerError("Aspect matcher does nothing")
    }

    fun match(resource: Resource): Boolean {
        return if (resource.aspectConversion.keys
                        .map { obj: Aspect -> obj.name }
                        .any { name: String -> name == coreName }
        ) false
        else labeler.isSuitable(resource.genome)
    }

    fun getResults(resource: Resource, resourcePool: ResourcePool): List<Pair<Resource, Int>> {
        return results
                .map { (first, second) -> Pair(
                        if (first == "MATCHED") resource else resourcePool.get(first),
                        second
                ) }
    }
}