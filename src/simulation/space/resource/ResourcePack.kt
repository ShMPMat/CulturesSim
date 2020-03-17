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

    protected fun internalAdd(resource: Resource) {
        if (resource.amount == 0) {
            return
        }
        val internal = resourceMap[resource]
        if (internal == null)
            resourceMap[resource] = resource
        else
            internal.merge(resource)
    }

    fun getResources(predicate: (Resource) -> Boolean) = ResourcePack(resources.filter(predicate))

    fun getResourcesWithTag(tag: ResourceTag): ResourcePack =
            ResourcePack(resources.filter { it.tags.contains(tag) })

    fun getPackedResource(resource: Resource): ResourcePack {//TODO remove
        val resourceInMap = resourceMap[resource] ?: return ResourcePack()
        return ResourcePack(setOf(resourceInMap))
    }

    fun getResource(resource: Resource): Resource = resourceMap[resource] ?: resource.copy(0)

    fun getAmount(tag: ResourceTag) = getResourcesWithTag(tag).amount

    fun getAmount(resource: Resource) = getResource(resource).amount

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