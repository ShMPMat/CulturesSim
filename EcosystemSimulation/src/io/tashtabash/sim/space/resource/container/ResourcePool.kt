package io.tashtabash.sim.space.resource.container

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.ResourceCore


class ResourcePool(val cores: List<ResourceCore>) {
    private val baseNameMap: Map<String, ResourceCore> =
            cores.associateBy { it.genome.baseName }

    val simpleNameMap: Map<String, List<ResourceCore>> =
            cores.groupBy { it.genome.name }

    fun get(predicate: (ResourceCore) -> Boolean): Resource? =
            cores.firstOrNull(predicate)?.largeSample?.copy()

    fun getAll(predicate: (ResourceCore) -> Boolean): List<Resource> =
            cores.filter(predicate).map { it.largeSample.copy() }

    fun getBaseNameOrNull(name: String): Resource? =
            baseNameMap[name]?.largeSample?.copy()

    fun getBaseName(name: String): Resource =
            getBaseNameOrNull(name)
                    ?: throw NoSuchElementException("No resource with name $name")

    fun getSimpleName(name: String): Resource =
            get { it.genome.name == name }
                    ?: throw NoSuchElementException("No resource with name $name")

    val all: List<Resource> = cores.map { it.largeSample }
}
