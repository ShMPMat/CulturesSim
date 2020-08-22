package simulation.space.territory

import simulation.space.resource.Resource
import simulation.space.tile.Tile


interface Territory {
    val tiles: Set<Tile>
    val outerBrink: Set<Tile>
    val innerBrink: List<Tile>
    val center: Tile?

    val size: Int
        get() = tiles.size

    val isEmpty: Boolean
        get() = size == 0

    val isNotEmpty: Boolean
        get() = !isEmpty

    val differentResources: Collection<Resource>
        get() = tiles
                .flatMap { it.resourcePack.resources }
                .distinct()

    val minTemperature: Int?
        get() = tiles
                .map(Tile::temperature)
                .min()

    fun filter(predicate: (Tile) -> Boolean) = tiles.filter(predicate)
    fun filterOuterBrink(predicate: (Tile) -> Boolean) = outerBrink.filter(predicate)
    fun filterInnerBrink(predicate: (Tile) -> Boolean) = innerBrink.filter(predicate)

    operator fun contains(tile: Tile?) = tiles.contains(tile)

    fun getResourceInstances(resource: Resource) = tiles
            .flatMap { it.resourcePack.getResource(resource).resources }

    fun getMostUselessTile(mapper: (Tile) -> Int): Tile? = tiles.minBy { mapper(it) }

    fun getMostUsefulTileOnOuterBrink(predicate: (Tile) -> Boolean, mapper: (Tile) -> Int): Tile? =
            filterOuterBrink(predicate)
                    .map { it to mapper(it) }
                    .maxBy { it.second }
                    ?.first
}
