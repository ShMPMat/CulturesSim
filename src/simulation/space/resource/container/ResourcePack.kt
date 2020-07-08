package simulation.space.resource.container

import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import java.util.*

open class ResourcePack(resources: Collection<Resource> = listOf()) {
    protected var resourceMap = mutableMapOf<Resource, Resource>()

    init {
        resources.forEach { internalAdd(it) }
    }

    val resources: List<Resource>
        get() = resourceMap.values.toList()

    val amount: Int
        get() = resources
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

    fun getResources(predicate: (Resource) -> Boolean) = ResourcePack(resources.filter(predicate))

    fun getResources(tag: ResourceTag) = ResourcePack(resources.filter { it.tags.contains(tag) })

    fun getResource(resource: Resource): ResourcePack {
        val resourceInMap = resourceMap[resource] ?: return ResourcePack()
        return ResourcePack(setOf(resourceInMap))
    }

    fun getUnpackedResource(resource: Resource): Resource = resourceMap[resource] ?: resource.copy(0)

    fun getAmount(tag: ResourceTag) = getResources(tag).amount

    fun getAmount(resource: Resource) = getUnpackedResource(resource).amount

    fun getAmount(predicate: (Resource) -> Boolean) = getResources(predicate).amount

    fun clearEmpty() = resourceMap.entries
            .filter { it.value.amount == 0 }
            .forEach { resourceMap.remove(it.key) }

    fun any(predicate: (Resource) -> Boolean) = resources.any(predicate)

    fun contains(resource: Resource) = resourceMap[resource] != null

    fun containsAll(resources: Collection<Resource>) = resources.all { contains(it) }

    fun containsAll(pack: ResourcePack) = containsAll(pack.resources)

    override fun toString() = resources.joinToString("\n") { "${it.fullName} ${it.amount};" }
}
