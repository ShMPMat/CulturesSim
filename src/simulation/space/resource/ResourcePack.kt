package simulation.space.resource

import simulation.space.resource.tag.ResourceTag
import java.util.*

open class ResourcePack(resources: Collection<Resource> = listOf()) {
    protected var resourceMap: MutableMap<Resource, Resource> = HashMap()

    init {
        resources.forEach { internalAdd(it) }
    }

    val resources: List<Resource>
        get() = ArrayList(resourceMap.values)

    val amount: Int
        get() = resources
                .map { it.amount }
                .fold(0, Int::plus)

    val isEmpty: Boolean
        get() = amount == 0

    val isNotEmpty: Boolean
        get() = !isEmpty

    protected fun internalAdd(resource: Resource): Boolean {
        if (resource.amount == 0) {
            return false
        }
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

    fun getResources(tag: ResourceTag): ResourcePack =
            ResourcePack(resources.filter { it.tags.contains(tag) })

    fun getResource(resource: Resource): ResourcePack {
        val resourceInMap = resourceMap[resource] ?: return ResourcePack()
        return ResourcePack(setOf(resourceInMap))
    }

    fun getUnpackedResource(resource: Resource): Resource = resourceMap[resource] ?: resource.copy(0)

    fun getAmount(tag: ResourceTag) = getResources(tag).amount

    fun getAmount(resource: Resource) = getUnpackedResource(resource).amount

    fun getAmount(predicate: (Resource) -> Boolean) = getResources(predicate).amount

    fun clearEmpty() {
        resourceMap.filter { it.value.amount == 0 }
                .forEach { resourceMap.remove(it.key) }
    }

    fun any(predicate: (Resource) -> Boolean) = resources.any(predicate)

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        for (resource in resources) {
            stringBuilder.append("${resource.fullName} ${resource.amount}; \n")
        }
        return stringBuilder.toString()
    }

    fun contains(resource: Resource) = resourceMap[resource] != null

    fun containsAll(resources: Collection<Resource>) = resources.all { contains(it) }

    fun containsAll(pack: ResourcePack) = containsAll(pack.resources)
}