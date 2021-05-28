package shmp.simulation.space.resource.container

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.ResourceTag
import java.util.*


open class ResourcePack(resources: Collection<Resource> = listOf()) {
    protected var resourceMap = TreeMap<Resource, Resource>()

    init {
        resources.forEach { internalAdd(it) }
    }

    val resources: List<Resource>
        get() = resourceMap.values.toList()

    val resourcesIterator: Iterator<Resource>
        get() = resourceMap.navigableKeySet().iterator()

    val amount: Int
        get() = resourceMap.navigableKeySet()
                .map { it.amount }
                .fold(0, Int::plus)

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

    fun getResources(predicate: (Resource) -> Boolean) =
            ResourcePack(resourceMap.navigableKeySet().filter(predicate))

    fun getResources(tag: ResourceTag) =
            ResourcePack(resourceMap.navigableKeySet().filter { it.tags.contains(tag) })

    fun getResource(resource: Resource): ResourcePack {
        val resourceInMap = resourceMap[resource]
                ?: return ResourcePack()
        return ResourcePack(listOf(resourceInMap))
    }

    fun getUnpackedResource(resource: Resource): Resource = resourceMap[resource] ?: resource.copy(0)

    fun getAmount(tag: ResourceTag) = getResources(tag).amount

    fun getTagPresence(tag: ResourceTag) = getResources(tag).resources
            .map { it.getTagPresence(tag) }
            .sum()

    fun getAmount(resource: Resource) = getUnpackedResource(resource).amount

    fun getAmount(predicate: (Resource) -> Boolean) = getResources(predicate).amount

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
