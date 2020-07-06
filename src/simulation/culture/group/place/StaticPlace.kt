package simulation.culture.group.place

import simulation.Controller
import simulation.space.resource.*
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.container.ResourcePack
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

    val _debugAdd = mutableListOf<Pair<Resource, String>>()
    fun addResource(resource: Resource) {
        if (tile.x == 30 && tile.y == 60) {
            val k = 0
        }
        if (resource.isEmpty) return
        if (_owned.any { it.ownershipMarker != ownershipMarker }) {
            val j = 0 //TODO return breakpoint and deal with it
        }
        resource.ownershipMarker = ownershipMarker
        _owned.add(resource)
        maxAmounts[resource] = max(maxAmounts[resource] ?: 0, _owned.getResource(resource).amount)
        _debugAdd.add(resource to Controller.session.world.getStringTurn())
        tile.addDelayedResource(_owned.getUnpackedResource(resource))
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
