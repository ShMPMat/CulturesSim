package shmp.simulation.space.territory

import shmp.simulation.space.tile.Tile


open class BrinkInvariantTerritory(tiles: Collection<Tile> = listOf()): MutableTerritory {
    private val _tiles = mutableSetOf<Tile>()
    override val tiles: Set<Tile>
        get() = _tiles

    private val _outerBrink = mutableSetOf<Tile>()
    override val outerBrink: Set<Tile>
        get() = _outerBrink

    override var center: Tile? = null

    override val innerBrink: List<Tile>
        get() = _outerBrink
                .flatMap { t -> t.getNeighbours { n -> this.contains(n) } }
                .distinct()

    init {
        tiles.forEach(this::add)
    }

    override fun add(tile: Tile?) {
        tile ?: return

        if (!_tiles.contains(tile)) {
            _tiles.add(tile)
            _outerBrink.remove(tile)
            tile.neighbours.forEach { addToOuterBrink(it) }
        }

        if (tiles.size == 1)
            center = tile
    }

    override fun remove(tile: Tile?) {
        if (tile == null)
            return

        if (!_tiles.remove(tile))
            return

        if (tile.getNeighbours { contains(it) }.isNotEmpty())
            addToOuterBrink(tile)

        tile.neighbours.forEach { t ->
            if (t.neighbours.none { this.contains(it) })
                _outerBrink.remove(t)
        }
    }

    private fun addToOuterBrink(tile: Tile) {
        if (!tiles.contains(tile))
            _outerBrink.add(tile)
    }
}
