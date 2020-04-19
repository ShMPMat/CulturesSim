package simulation.culture.aspect.dependency

import simulation.space.resource.tag.ResourceTag

data class AspectDependencies(val map: MutableMap<ResourceTag, MutableSet<Dependency>>) {
    val size: Int
        get() = map.size

    val phony: MutableSet<Dependency>
        get() = map.getValue(ResourceTag.phony())

    val nonPhony: Map<ResourceTag, MutableSet<Dependency>>
        get() = map.filter { it.key != ResourceTag.phony()  }.toMap()

    fun containsDependency(tag: ResourceTag) = map.containsKey(tag)
}
