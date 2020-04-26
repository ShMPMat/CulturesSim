package simulation.culture.group

import simulation.space.resource.MutableResourcePack
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

    fun addResources(pack: ResourcePack) {
        _owned.addAll(pack)
        tile.addDelayedResources(pack)
    }
}