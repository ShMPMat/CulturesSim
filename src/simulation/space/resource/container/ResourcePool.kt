package simulation.space.resource.container

import simulation.space.resource.Resource
import simulation.space.resource.ResourceIdeal

class ResourcePool(private val resources: List<ResourceIdeal>) {
    fun get(predicate: (Resource) -> Boolean) = resources.firstOrNull(predicate)?.copy()

    fun getAll(predicate: (Resource) -> Boolean) = resources
            .filter(predicate)
            .map { it.copy() }

    fun getBaseName(name: String) = get { it.baseName == name }
            ?: throw NoSuchElementException("No resource with name $name")

    fun getSimpleName(name: String) = get { it.simpleName == name }
            ?: throw NoSuchElementException("No resource with name $name")

    val all: List<Resource>
        get() = resources
}
