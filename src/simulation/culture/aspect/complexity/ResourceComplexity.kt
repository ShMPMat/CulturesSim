package simulation.culture.aspect.complexity

import simulation.space.resource.Resource

interface ResourceComplexity {
    fun getComplexity(resource: Resource): Double
}