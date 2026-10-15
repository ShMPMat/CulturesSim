package io.tashtabash.sim.space.generator

import io.tashtabash.random.randomElement
import io.tashtabash.random.randomElementOrNull
import io.tashtabash.random.randomTile
import io.tashtabash.sim.space.Extent
import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.TectonicPlate
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.resource.container.ResourcePool
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.updater.FlowTransferUpdater
import io.tashtabash.sim.space.tile.updater.FlowUpdater
import io.tashtabash.sim.space.tile.updater.MeteorStrike
import io.tashtabash.sim.space.tile.updater.TileUpdater
import io.tashtabash.sim.space.tile.updater.TypeUpdater
import kotlin.math.ceil
import kotlin.random.Random
import kotlin.time.measureTime


fun generateMap(parameters: GenerationParameters, resourcePool: ResourcePool, random: Random): WorldMap {
    val scale = parameters.scale
    val tiles = createTiles(parameters.sizeX, parameters.sizeY, scale) { createTileUpdaters(resourcePool) }
    val map = WorldMap(tiles, Extent(parameters.sizeX, parameters.sizeY, data.xMapLooping, data.yMapLooping), scale)
    setTileNeighbours(map)
    println("Start generating plates")
    println("Finished generating plates in:" + measureTime {
        val tectonicPlates = randomPlates(parameters, map, random)
        tectonicPlates.forEach { map.addPlate(it) }
    })
    fill(map)
    setUpParallelUpdate(map, resourcePool)

    return map
}

fun createTileUpdaters(resourcePool: ResourcePool): MutableList<TileUpdater> = mutableListOf(
    TypeUpdater(resourcePool.getBaseName("Water")),
    MeteorStrike(resourcePool.getBaseName("RawIron")),
    FlowTransferUpdater(resourcePool.getBaseName("Water")),
    FlowUpdater()
)

internal fun setUpParallelUpdate(map: WorldMap, resourcePool: ResourcePool) {
    val maxSpeed = resourcePool.all.maxOf { it.genome.behaviour.tileSpeed(map.scale) }
    map.tileUpdateOrder = map.calculateTileUpdateOrder(ceil(maxSpeed + 1).toInt())
}

internal fun setTileNeighbours(map: WorldMap) {
    for (i in map.xCoordinates)
        for (j in map.yCoordinates)
            map[i, j]?.let { tile ->
                val neighbours = listOfNotNull(
                    map[i, j + 1],
                    map[i, j - 1],
                    map[i + 1, j],
                    map[i - 1, j],
                ).distinct()

                tile.setNeighbours(neighbours.map { it to map.direction(tile, it)!! })
            }
}

private fun createTiles(x: Int, y: Int, scale: Scale, createUpdaters: () -> MutableList<TileUpdater>): List<Tile> {
    val tiles = mutableListOf<Tile>()

    for (i in 0 until x)
        for (j in 0 until y)
            tiles += Tile(i * y + j, i, j, createUpdaters(), scale)

    return tiles
}

private fun randomPlates(parameters: GenerationParameters, map: WorldMap, random: Random): List<TectonicPlate> {
    val tectonicPlates = mutableListOf<TectonicPlate>()
    val frontiers = mutableListOf<PlateFrontier>()
    for (i in 0 until parameters.platesAmount) {
        val direction = randomElement(Direction.entries, random)
        val type = randomElement(TectonicPlate.Type.entries, random)
        val tectonicPlate = TectonicPlate(direction, type, parameters, i.toByte())
        var tile = randomTile(map)
        while (tile.plate != null)
            tile = randomTile(map)

        tectonicPlate.add(tile)
        tectonicPlates += tectonicPlate
        frontiers += PlateFrontier()
    }
    for (i in tectonicPlates.indices)
        for (neighbour in tectonicPlates[i].center?.neighbours ?: listOf())
            frontiers[i].add(neighbour)

    val growingIndices = tectonicPlates.indices.toMutableList()
    while (growingIndices.isNotEmpty()) {
        var g = 0
        while (g < growingIndices.size) {
            val i = growingIndices[g]
            val tile = frontiers[i].random(random)
            if (tile == null) {
                growingIndices.removeAt(g)
                continue
            }

            for (f in frontiers)
                f.remove(tile)

            tectonicPlates[i].add(tile)
            for (neighbour in tile.neighbours)
                frontiers[i].add(neighbour)
            g++
        }
    }
    return tectonicPlates
}

private fun fill(map: WorldMap) {
    map.tectonicPlates.getOrNull(0)?.type = TectonicPlate.Type.Terrain
    map.tectonicPlates.getOrNull(1)?.type = TectonicPlate.Type.Oceanic

    for (plate in map.tectonicPlates)
        plate.initialize()
    println("Moved plates in:" + measureTime {
        map.platesUpdate()
    })
}

private class PlateFrontier {
    private val tiles = mutableListOf<Tile>()
    private val index = mutableMapOf<Tile, Int>()

    fun add(tile: Tile) {
        if (tile.plate != null || tile in index)
            return

        index[tile] = tiles.size
        tiles += tile
    }

    fun remove(tile: Tile) {
        val i = index.remove(tile)
            ?: return

        val last = tiles.removeAt(tiles.lastIndex) // swap w/ the last to not change the list for O(n)
        if (i < tiles.size) {
            tiles[i] = last
            index[last] = i
        }
    }

    fun random(random: Random): Tile? =
        randomElementOrNull(tiles, random)
}
