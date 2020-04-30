package simulation.space.resource

import simulation.space.tile.Tile
import simulation.space.resource.tag.ResourceTag
import java.util.*
import kotlin.math.min

class MutableResourcePack(resources: Collection<Resource> = emptyList()) : ResourcePack(resources) {
    constructor(pack: ResourcePack) : this(pack.resources)

    fun getResourceAndRemove(resource: Resource): ResourcePack {
        val innerResource = getResource(resource)
        innerResource.resources.forEach { this.remove(it) }
        return innerResource
    }

    fun getResourcePartAndRemove(resource: Resource, amount: Int): ResourcePack {
        val innerResource = getResource(resource)
        val result = MutableResourcePack()
        var resultAmount = 0
        for (res in innerResource.resources) {
            val resAmount = min(res.amount, amount - resultAmount)
            resultAmount += resAmount
            if (resAmount == res.amount) {
                result.add(res)
                remove(res)
            } else {
                result.add(resource.getCleanPart(resAmount))
            }
        }
        return result
    }

    fun getResourcePart(resource: Resource, ceiling: Int): MutableResourcePack =
            getPart(getResource(resource), ceiling)

    private fun getPart(pack: ResourcePack, amount: Int): MutableResourcePack {
        val result = MutableResourcePack()
        var counter = 0
        for (resource in pack.resources) {
            if (counter >= amount)
                break
            result.add(resource)
            counter += resource.amount
        }
        return result
    }

    fun getAmountOfResourcesWithTagAndErase(tag: ResourceTag, amount: Int): Pair<Int, List<Resource>> { //TODO looks bad
        val innerResource: Collection<Resource> = getResources(tag).resources
        val result: MutableList<Resource> = ArrayList()
        var counter = 0
        for (resource in innerResource) {
            if (counter >= amount) {
                break
            }
            result.add(resource)
            counter += resource.amount
        }
        result.forEach { this.remove(it) }
        return Pair<Int, List<Resource>>(counter, result)
    }

    fun disbandOnTile(tile: Tile) {
        tile.addDelayedResources(resources.filter {  it.genome.isMovable })
        resourceMap.clear()
    }

    /**
     * Returns true only if the Resource was merged
     */
    fun add(resource: Resource): Boolean = internalAdd(resource)

    fun addAll(resources: Collection<Resource>) = resources.forEach { add(it) }

    fun addAll(resourcePack: ResourcePack) = addAll(resourcePack.resources)

    fun remove(resource: Resource) = resourceMap.remove(resource)

    fun removeAll(resources: Collection<Resource>) = resources.forEach { this.remove(it) }

    fun removeAll(pack: ResourcePack) = removeAll(pack.resources)

    fun destroyAllResourcesWithTag(tag: ResourceTag) {
        val result = getResources(tag)
        removeAll(result)
        result.resources.forEach { it.destroy() }
    }
}