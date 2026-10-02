package io.tashtabash.sim.space

import io.tashtabash.random.singleton.RandomSingleton.random
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.territory.BrinkInvariantTerritory
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.abs


class TectonicPlate(val direction: Direction, var type: Type) : BrinkInvariantTerritory() {
    init {
        require(direction != Direction.Here) { "TectonicPlate must move to a side" }
    }

    // Whether it was ever moved.
    private var isMoved = false

    /**
     * Which Tiles are affected by this Plate movement.
     */
    val affectedTiles: List<Pair<Tile, Double>> by lazy {
        // The Tiles in front of the Plate in the direction of its movement
        val startTiles = filterOuterBrink { tile: Tile ->
            tile.anyNeighbourIn(direction.opposite) { it.plate === this }
        }

        val tiles = mutableListOf<Tile>()
        for (tile in startTiles) {
            val neighbours = mutableListOf<Tile>()
            val newTiles = mutableListOf<Tile>()
            val plate = tile.plate ?: error("Plate for ${tile.x} ${tile.y} isn't set")
            neighbours += tile
            newTiles += tile
            for (i in 0 until getInteractionCoefficient(plate)) {
                for (n in neighbours)
                    newTiles += n.getNeighbours { !newTiles.contains(it) }
                neighbours.clear()
                neighbours += newTiles
            }
            tiles += neighbours
        }

        tiles.map { it to (random.nextDouble() + .1) / 1.1 }
    }

    /**
     * Changes Plate's Tiles depending on its Type.
     */
    fun initialize() {
        if (type == Type.Terrain)
            return

        for (tile in tiles) {
            tile.setType(Tile.Type.Water, true)
            tile.addDelayedResource(data.resourcePool.getBaseName("SaltWater"))
        }
    }

    override fun add(tile: Tile?) {
        super.add(tile)
        tile!!.plate = this
    }

    private fun getInteractionCoefficient(tectonicPlate: TectonicPlate): Int {
        val x = abs(direction.dx - tectonicPlate.direction.dx)
        val y = abs(direction.dy - tectonicPlate.direction.dy)

        return if (x == 0 && y == 0)
            0
        else if (x <= 1 && y <= 1)
            data.tectonicRange / 2
        else
            data.tectonicRange
    }

    /**
     * Moves plate in its direction and changes landscape.
     */
    fun move() {
        if (isMoved)
            .7.chanceOf {
                return
            }

        val rise = data.minTectonicRise
        for ((tile, p) in affectedTiles) {
            p.chanceOf {
                tile.setLevel(
                    if (isMoved)
                        tile.level + 1
                    else
                        tile.level + rise + random.nextInt(rise)
                )
            }
        }
        isMoved = true
    }

    enum class Type {
        Terrain, Oceanic
    }
}
