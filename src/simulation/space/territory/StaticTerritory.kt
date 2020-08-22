package simulation.space.territory

import simulation.space.tile.Tile


open class StaticTerritory(tiles: Collection<Tile> = setOf()) : Territory {
    override val tiles = tiles.toSet()

    override val outerBrink: Set<Tile>
        get() = tiles
                .flatMap { t -> t.getNeighbours { it !in tiles } }
                .toSet()

    override val center: Tile?
        get() = tiles.firstOrNull()

    override val innerBrink: List<Tile>
        get() = outerBrink
                .flatMap { t -> t.getNeighbours { it !in tiles } }
                .distinct()
}
