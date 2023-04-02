package io.tashtabash.simulation.culture.aspect.complexity

import io.tashtabash.simulation.space.resource.Resource

interface ResourceComplexity {
    fun getComplexity(resource: Resource): Double
}