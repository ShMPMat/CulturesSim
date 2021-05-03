package shmp.simulation.space.tile

import java.util.*


fun setTags(tile: Tile, name: String): Boolean {
    if (tile.type == Tile.Type.Mountain
            && tile.tagPool.getByType(getMountainTag("").type).isEmpty()
            && tile.tagPool.getByType(getMountainsTag("").type).isEmpty()) {
        val tiles = getArea(tile) { it.type == Tile.Type.Mountain}
        if (tiles.size == 1)
            tile.tagPool.add(getMountainTag(name))
        else {
            val tag = getMountainsTag(name)
            tiles.forEach { it.tagPool.add(tag) }
        }
        return true
    }
    return false
}

private fun getArea(startTile: Tile, predicate: (Tile) -> Boolean): Collection<Tile> {
    val queue: Queue<Tile> = ArrayDeque()
    val goodTiles = mutableSetOf<Tile>()
    queue.add(startTile)
    while (queue.isNotEmpty()) {
        val currentTile = queue.poll()
        goodTiles.add(currentTile)
        queue.addAll(
                currentTile.getNeighbours { predicate(it) && !queue.contains(it) && !goodTiles.contains(it) }
        )
    }
    return goodTiles
}
