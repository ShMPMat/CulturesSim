package simulation.culture.group

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import simulation.space.tile.Tile
import simulation.space.tile.TileTag

class Place(val tile: Tile, val tileTag: TileTag) {
    private val _owned = MutableResourcePack()
    val owned: ResourcePack
        get() = _owned

    init {
        tile.tagPool.add(tileTag)
    }

    fun delete() {
        tile.tagPool.remove(tileTag)
    }

    fun addResources(pack: ResourcePack) = pack.resources.forEach(this::addResource)

    fun addResource(resource: Resource) {
        _owned.add(resource)
        tile.addDelayedResource(resource)
    }

    override fun toString(): String {
        return "Place on ${tile.x} ${tile.y}, ${tileTag.name}, resources:" +
                _owned.resources.joinToString { it.fullName + ":" + it.amount }
    }
}