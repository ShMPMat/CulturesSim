package simulation.culture.aspect.complexity

import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag

class ResourceTagComplexity(private val tag: ResourceTag) : ResourceComplexity {
    override fun getComplexity(resource: Resource): Double {
        val result = resource.getTagLevel(tag).toDouble()
        return if (result == 0.0) 1.0 else result
    }
}