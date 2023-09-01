package io.tashtabash.sim.culture.aspect.complexity

import io.tashtabash.sim.space.resource.Resource

interface ResourceComplexity {
    fun getComplexity(resource: Resource): Double
}