package shmp.simulation.culture.aspect.complexity

import shmp.simulation.space.resource.Resource

interface ResourceComplexity {
    fun getComplexity(resource: Resource): Double
}