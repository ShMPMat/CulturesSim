package io.tashtabash.simulation.space.territory

import io.tashtabash.simulation.space.tile.Tile


class StaticTerritory(override val tiles: Set<Tile> = setOf()) : Territory {
    override val outerBrink by lazy {
        tiles.flatMap { t -> t.getNeighbours { it !in tiles } }.toSet()
    }

    override val center = tiles.firstOrNull()

    override val innerBrink by lazy {
        outerBrink
            .flatMap { t -> t.getNeighbours { it !in tiles } }
            .distinct()
    }
}
