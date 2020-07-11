package simulation.culture.group.place

import simulation.space.resource.OwnershipMarker
import simulation.space.resource.Resource
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.free
import simulation.space.resource.freeCopy
import simulation.space.tile.Tile
import simulation.space.tile.TileTag
import kotlin.math.max

open class StaticPlace(val tile: Tile, val tileTag: TileTag) {
    private val _owned = MutableResourcePack()
    private val maxAmounts = mutableMapOf<Resource, Int>()
    val owned: ResourcePack
        get() = _owned

    init {
        tile.tagPool.add(tileTag)
    }

    val ownershipMarker = OwnershipMarker(tileTag.name)

    fun delete() {
        tile.tagPool.remove(tileTag)
    }

    fun addResource(resource: Resource) {
        if (resource.isEmpty)
            return
        if (_owned.any { it.ownershipMarker != ownershipMarker }) {
            val j = 0//TODO remove someday
        }
        val ownedResource = resource.exactCopyAndDestroy(ownershipMarker = ownershipMarker)
        _owned.add(ownedResource)
        maxAmounts[ownedResource] = max(maxAmounts[ownedResource] ?: 0, _owned.getResource(ownedResource).amount)
        tile.addDelayedResource(_owned.getUnpackedResource(ownedResource))
    }

    fun addResources(resources: Collection<Resource>) = resources.forEach(this::addResource)

    fun addResources(pack: ResourcePack) = addResources(pack.resources)

    fun getLacking() : List<Resource> {
        return maxAmounts
                .map { (r, a) ->  r to max(a - _owned.getUnpackedResource(r).amount, 0)}
                .filter { it.second > 0 }
                .map { (r, a) -> r.copy(a) }
    }

    fun takeResource(resource: Resource, amount: Int): ResourcePack {
        val remapedResource = resource.copy(ownershipMarker = ownershipMarker)
        val result = _owned.getResourcePartAndRemove(remapedResource, amount)

        if (result.contains(remapedResource))
            maxAmounts[remapedResource] = max(0, (maxAmounts[remapedResource] ?: 0) - result.amount)

        return ResourcePack(result.resources.map { free(it) })
    }

    fun getResourcesAndRemove(predicate: (Resource) -> Boolean) =
            ResourcePack(_owned.getResourcesAndRemove { predicate(freeCopy(it)) }.resources.map { free(it) })

    fun contains(resource: Resource) = _owned.contains(resource.copy(ownershipMarker = ownershipMarker))

    override fun toString() = "Place on ${tile.x} ${tile.y}, ${tileTag.name}, resources:" +
            _owned.resources.joinToString { it.fullName + ":" + it.amount }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StaticPlace

        if (tile != other.tile) return false
        if (tileTag != other.tileTag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tile.hashCode()
        result = 31 * result + tileTag.hashCode()
        return result
    }
}
