package io.tashtabash.simulation.space.resource.container

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.tag.ResourceTag
import java.util.*


open class ResourcePack private constructor(resources: Collection<Resource>, doSafeAdd: Boolean) {
    protected var resourceMap = TreeMap<Resource, Resource>()

    init {
        if (doSafeAdd)
            resources.forEach { internalAdd(it) }
        else
            resources.forEach{ resourceMap[it] = it }
    }

    constructor(resources: Collection<Resource> = listOf()): this(resources, true)

    val resources: List<Resource>
        get() = resourceMap.navigableKeySet().toList()

    val resourcesIterator: Iterator<Resource>
        get() = resourceMap.navigableKeySet().iterator()

    val amount: Int
        get() = resourceMap.navigableKeySet().sumBy { it.amount }

    val isEmpty: Boolean
        get() = amount == 0

    val isNotEmpty: Boolean
        get() = !isEmpty

    // returns true if resource has been merged
    protected fun internalAdd(resource: Resource): Boolean {
        if (resource.amount == 0)
            return false

        val internal = resourceMap[resource]

        return if (internal == null) {
            resourceMap[resource] = resource
            false
        } else {
            internal.merge(resource)
            true
        }
    }

    fun getResourcesUnpacked(predicate: (Resource) -> Boolean) =
            resourceMap.navigableKeySet().filter(predicate)

    fun getTaggedResourcesUnpacked(tag: ResourceTag) =
            resourceMap.navigableKeySet().filter { it.genome.getTagLevel(tag) > 0 }

    fun getResources(predicate: (Resource) -> Boolean) =
            ResourcePack(getResourcesUnpacked(predicate), false)

    fun getResource(resource: Resource): ResourcePack {
        val resourceInMap = resourceMap[resource]
                ?: return ResourcePack()
        return ResourcePack(listOf(resourceInMap), false)
    }

    fun getUnpackedResource(resource: Resource): Resource = resourceMap[resource] ?: resource.copy(0)

    fun getTagPresence(tag: ResourceTag) = getTaggedResourcesUnpacked(tag)
            .map { it.getTagPresence(tag) }
            .sum()

    fun getAmount(tag: ResourceTag) = getTaggedResourcesUnpacked(tag).sumBy { it.amount }

    fun getAmount(resource: Resource) = resourceMap[resource]?.amount ?: 0

    fun getAmount(predicate: (Resource) -> Boolean) = getResourcesUnpacked(predicate).sumBy { it.amount }

    fun clearEmpty() = resourceMap.entries
            .filter { it.value.amount == 0 }
            .forEach { resourceMap.remove(it.key) }

    fun any(predicate: (Resource) -> Boolean) = resourceMap.navigableKeySet().any(predicate)

    fun contains(resource: Resource) = resourceMap[resource] != null

    fun containsAll(resources: Collection<Resource>) = resources.all { contains(it) }

    fun containsAll(pack: ResourcePack) = containsAll(pack.resourceMap.navigableKeySet())

    override fun toString() =
            resourceMap.navigableKeySet().joinToString("\n") { "${it.fullName} ${it.amount};" }

    val listResources
        get() = resourceMap.navigableKeySet().joinToString { "${it.fullName} ${it.amount};" }
}
