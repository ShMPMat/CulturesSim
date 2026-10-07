package io.tashtabash.sim.space

import io.tashtabash.random.singleton.RandomSingleton.random
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.generator.GenerationParameters
import io.tashtabash.sim.space.territory.BrinkInvariantTerritory
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.abs
import kotlin.math.max


class TectonicPlate(
    val direction: Direction,
    var type: Type,
    private val parameters: GenerationParameters
) : BrinkInvariantTerritory() {
    val affectedTiles: List<Pair<Tile, Double>> by lazy {
        // The Tiles in front of the Plate in the direction of its movement
        val startTiles = filterOuterBrink { tile: Tile ->
            tile.anyNeighbourIn(direction.opposite) { it.plate === this }
        }

        val tiles = mutableSetOf<Tile>()
        for (tile in startTiles) {
            val plate = tile.plate
                ?: error("Plate for ${tile.x} ${tile.y} isn't set")
            tiles += tile
            tiles += tile.getTilesInRadius(getInteractionCoefficient(plate))
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
        tile?.plate = this
    }

    private fun getInteractionCoefficient(tectonicPlate: TectonicPlate): Int {
        val x = abs(direction.dx - tectonicPlate.direction.dx)
        val y = abs(direction.dy - tectonicPlate.direction.dy)

        val vectorCoefficient = if (x == 0 && y == 0)
            0
        else if (x <= 1 && y <= 1)
            parameters.tectonicRange / 2
        else
            parameters.tectonicRange

        return if (type != tectonicPlate.type)
            max(1, vectorCoefficient / 2)
        else
            vectorCoefficient
    }

    fun move() {
        .7.chanceOf {
            return
        }

        for ((tile, p) in affectedTiles)
            p.chanceOf {
                tile.setLevel(tile.level + 1)
            }
    }

    enum class Type {
        Terrain, Oceanic
    }
}
