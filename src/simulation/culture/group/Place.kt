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