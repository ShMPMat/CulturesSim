package simulation.culture.aspect.dependency

import simulation.space.resource.tag.ResourceTag

data class AspectDependencies(val map: MutableMap<ResourceTag, MutableSet<Dependency>>) {
    val size: Int
        get() = map.size
}
