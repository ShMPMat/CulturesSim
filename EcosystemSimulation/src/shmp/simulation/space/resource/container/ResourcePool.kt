package shmp.simulation.space.resource.container

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceIdeal


class ResourcePool(private val resources: List<ResourceIdeal>) {
    private val baseNameMap: Map<String, ResourceIdeal> =
            resources.associateBy { it.baseName }

    val simpleNameMap: Map<String, List<ResourceIdeal>> =
            resources.groupBy { it.simpleName }

    fun get(predicate: (Resource) -> Boolean): Resource? =
            resources.firstOrNull(predicate)?.copy()

    fun getAll(predicate: (Resource) -> Boolean): List<Resource> =
            resources.filter(predicate).map { it.copy() }

    fun getBaseNameOrNull(name: String): Resource? =
            baseNameMap[name]?.copy()

    fun getBaseName(name: String): Resource =
            getBaseNameOrNull(name)
                    ?: throw NoSuchElementException("No resource with name $name")

    fun getSimpleName(name: String): Resource =
            get { it.simpleName == name }
                    ?: throw NoSuchElementException("No resource with name $name")

    val all: List<Resource> = resources
}
