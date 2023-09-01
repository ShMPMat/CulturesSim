package io.tashtabash.sim.culture.aspect.complexity

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.ResourceTag

class ResourceTagComplexity(private val tag: ResourceTag) : ResourceComplexity {
    override fun getComplexity(resource: Resource): Double {
        val result = resource.getTagLevel(tag).toDouble()
        return if (result == 0.0) 1.0 else result
    }
}