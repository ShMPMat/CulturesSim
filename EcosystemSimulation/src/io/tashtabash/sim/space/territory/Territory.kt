package io.tashtabash.sim.space.territory

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.tile.Tile


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

    val allResources: Collection<Resource>
        get() = tiles.flatMap { it.resourcePack.resources }

    val allResourcesPack: ResourcePack
        get() = ResourcePack(allResources)

    val differentResources: Collection<Resource>
        get() = allResources.distinct()

    val minTemperature: Double?
        get() = tiles.map(Tile::temperature)
                .minOrNull()

    fun filter(predicate: (Tile) -> Boolean) = tiles.filter(predicate)
    fun filterOuterBrink(predicate: (Tile) -> Boolean) = outerBrink.filter(predicate)//TODO sort
    fun filterInnerBrink(predicate: (Tile) -> Boolean) = innerBrink.filter(predicate)

    operator fun contains(tile: Tile?) = tiles.contains(tile)

    fun getResourceInstances(resource: Resource) = tiles
            .flatMap { it.resourcePack.getResource(resource).resources }

    fun getMostUselessTile(mapper: (Tile) -> Int): Tile? = tiles.minByOrNull { mapper(it) }

    fun getMostUsefulTileOnOuterBrink(predicate: (Tile) -> Boolean, mapper: (Tile) -> Int): Tile? =
            filterOuterBrink(predicate)
                    .map { it to mapper(it) }
                    .maxByOrNull { it.second }
                    ?.first
}
