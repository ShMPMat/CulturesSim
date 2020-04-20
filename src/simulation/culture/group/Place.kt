package simulation.culture.group

import simulation.space.tile.Tile
import simulation.space.tile.TileTag

class Place(val tile: Tile, val tileTag: TileTag) {
    init {
        tile.tagPool.add(tileTag)
    }

    fun delete() {
        tile.tagPool.remove(tileTag)
    }
}