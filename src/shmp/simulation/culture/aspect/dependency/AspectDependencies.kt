package shmp.simulation.culture.aspect.dependency

import shmp.simulation.space.resource.tag.ResourceTag


data class AspectDependencies(val map: MutableMap<ResourceTag, MutableSet<Dependency>>) {
    val size: Int
        get() = map.size

    val phony: MutableSet<Dependency>
        get() = map.getValue(shmp.simulation.space.resource.tag.phony)

    val nonPhony: Map<ResourceTag, MutableSet<Dependency>>
        get() = map.filter { it.key != shmp.simulation.space.resource.tag.phony }.toMap()

    fun containsDependency(tag: ResourceTag) = map.containsKey(tag)

    fun removeIf(predicate: (Dependency) -> Boolean) = map.forEach {
        it.value.removeIf(predicate)
    }
}
