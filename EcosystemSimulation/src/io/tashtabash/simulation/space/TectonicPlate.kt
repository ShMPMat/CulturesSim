package io.tashtabash.simulation.space

import io.tashtabash.random.singleton.RandomSingleton.random
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.testProbability
import io.tashtabash.simulation.space.SpaceData.data
import io.tashtabash.simulation.space.territory.BrinkInvariantTerritory
import io.tashtabash.simulation.space.tile.Tile
import java.util.*
import kotlin.math.abs


class TectonicPlate(val direction: Direction, var type: Type) : BrinkInvariantTerritory() {
    //    Whether it was moved for the first time.
    private var isMoved = false

    /**
     * Which Tiles are affected by this Plate movement.
     */
    val affectedTiles: List<Pair<Tile, Double>> by lazy {
            val startTiles = filterOuterBrink { tile: Tile ->
                val affectedTiles = when (direction) {
                    Direction.U -> tile.getNeighbours { it.x == tile.x + 1 && it.y == tile.y }
                    Direction.R -> tile.getNeighbours { it.x == tile.x && it.y == tile.y - 1 }
                    Direction.L -> tile.getNeighbours { it.x == tile.x && it.y == tile.y + 1 }
                    Direction.D -> tile.getNeighbours { it.x == tile.x - 1 && it.y == tile.y }
                }
                affectedTiles.isNotEmpty() && affectedTiles[0].plate === this
            }

            val tiles = mutableListOf<Tile>()
            for (tile in startTiles) {
                val neighbours: MutableList<Tile> = ArrayList()
                val newTiles: MutableList<Tile> = ArrayList()
                val plate = tile.plate ?: error("Plate for ${tile.x} ${tile.y} isn't set")
                neighbours += tile
                newTiles += tile
                for (i in 0 until getInteractionCoefficient(plate)) {
                    for (n in neighbours)
                        newTiles += n.getNeighbours { !newTiles.contains(it) }
                    neighbours.clear()
                    neighbours.addAll(newTiles)
                }
                tiles += neighbours
            }

            tiles.map { it to (random.nextDouble() + 0.1) / 1.1 }
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
        val x = abs(direction.vector.first - tectonicPlate.direction.vector.first)
        val y = abs(direction.vector.second - tectonicPlate.direction.vector.second)

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
            0.7.chanceOf {
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

    enum class Direction(x: Int, y: Int) {
        U(-1, 0),
        D(1, 0),
        L(0, -1),
        R(0, 1);

        var vector = x to y
    }

    enum class Type {
        Terrain, Oceanic
    }
}
