package simulation.space

import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import simulation.space.tile.Tile
import java.util.*

open class Territory(tiles: Collection<Tile> = ArrayList()) {
    private val innerTiles = mutableSetOf<Tile>()
    val tiles: Set<Tile>
        get() = innerTiles

    private val innerOuterBrink = mutableSetOf<Tile>()
    val outerBrink: Set<Tile>
        get() = innerOuterBrink

    var center: Tile? = null

    init {
        addAll(tiles)
    }

    fun getTiles(predicate: (Tile) -> Boolean) = tiles.filter(predicate)

    fun getOuterBrink(predicate: (Tile) -> Boolean) = innerOuterBrink.filter(predicate)

    val innerBrink: List<Tile>
        get() = innerOuterBrink
                .flatMap { t -> t.getNeighbours { n -> this.contains(n) } }
                .distinct()

    fun getInnerBrink(predicate: (Tile) -> Boolean) = innerBrink.filter(predicate)

    fun getResourcesWithAspectTag(resourceTag: ResourceTag)= differentResources
            .filter { it.tags.contains(resourceTag) }

    val differentResources: Collection<Resource>
        get() = tiles
                .flatMap { it.resourcePack.resources }
                .distinct()

    val minTemperature: Int?
        get() = tiles
                .map(Tile::temperature)
                .min()

    val size: Int
        get() = tiles.size

    val isEmpty: Boolean
        get() = size == 0

    val isNotEmpty: Boolean
        get() = !isEmpty

    fun getResourceInstances(resource: Resource?) = tiles.flatMap { t ->
        t.resourcePack.getResources { r -> r == resource }.resources
    }

    operator fun contains(tile: Tile?) = tiles.contains(tile)

    fun addAll(territory: Territory) = addAll(territory.tiles)

    fun addAll(tiles: Collection<Tile>) = tiles.forEach { add(it) }

    open fun add(tile: Tile?) {
        if (tile == null)
            return

        if (!tiles.contains(tile)) {
            innerTiles.add(tile)
            innerOuterBrink.remove(tile)
            tile.neighbours.forEach { addToOuterBrink(it) }
        }

        if (tiles.size == 1)
            center = tile
    }

    private fun addToOuterBrink(tile: Tile) {
        if (!innerOuterBrink.contains(tile) && !tiles.contains(tile))
            innerOuterBrink.add(tile)
    }

    fun remove(tile: Tile?) {
        if (tile == null)
            return

        if (!innerTiles.remove(tile))
            return

        if (tile.getNeighbours { contains(it) }.isNotEmpty())
            addToOuterBrink(tile)

        tile.neighbours.forEach { t: Tile ->
            if (t.neighbours.none { this.contains(it) })
                innerOuterBrink.remove(t)
        }
    }

    fun removeAll(tiles: Collection<Tile?>) = tiles.forEach { remove(it) }

    fun removeIf(predicate: (Tile) -> Boolean) = removeAll(getTiles(predicate))

    fun getMostUselessTile(mapper: (Tile) -> Int): Tile? = tiles.minBy { mapper(it) }

    fun getMostUsefulTileOnOuterBrink(predicate: (Tile) -> Boolean, mapper: (Tile) -> Int): Tile? =
            getOuterBrink(predicate)
                    .map { it to mapper(it) }
                    .maxBy { it.second }
                    ?.first
}
