package simulation.culture.group

import simulation.space.resource.*
import simulation.space.tile.Tile
import simulation.space.tile.TileTag
import kotlin.math.max

class Place(val tile: Tile, val tileTag: TileTag) {
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
        if (_owned.any { it.ownershipMarker != ownershipMarker }) {
            val j = 0
        }
        resource.ownershipMarker = ownershipMarker
        _owned.add(resource)
        maxAmounts[resource] = max(maxAmounts[resource] ?: 0, _owned.getResource(resource).amount)
        tile.addDelayedResource(resource)
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
        val remapedResource = remapOwner(resource.copy(), ownershipMarker)
        val result = _owned.getResourcePartAndRemove(remapedResource, amount)
        if (result.contains(remapedResource))
            maxAmounts[remapedResource] = max(0, (maxAmounts[remapedResource] ?: 0) - result.amount)
        return ResourcePack(result.resources.map { free(it) })
    }

    fun getResourcesAndRemove(predicate: (Resource) -> Boolean): ResourcePack {
        return ResourcePack(_owned.getResourcesAndRemove(predicate).resources.map { free(it) })
    }

    override fun toString() = "Place on ${tile.x} ${tile.y}, ${tileTag.name}, resources:" +
            _owned.resources.joinToString { it.fullName + ":" + it.amount }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Place

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