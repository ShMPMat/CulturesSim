package io.tashtabash.sim.space.generator

import io.tashtabash.random.randomElement
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
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random


fun generateMap(parameters: GenerationParameters, resourcePool: ResourcePool, random: Random): WorldMap {
    val scale = parameters.scale
    val tiles = createTiles(parameters.sizeX, parameters.sizeY, scale) { createTileUpdaters(resourcePool) }
    val map = WorldMap(tiles, Extent(parameters.sizeX, parameters.sizeY, data.xMapLooping, data.yMapLooping))
    setTileNeighbours(map)
    val tectonicPlates = randomPlates(parameters, map, random)
    tectonicPlates.forEach { map.addPlate(it) }
    fill(map)
    setUpParallelUpdate(map, resourcePool, scale)

    return map
}

fun createTileUpdaters(resourcePool: ResourcePool): MutableList<TileUpdater> = mutableListOf(
    TypeUpdater(resourcePool.getBaseName("Water")),
    MeteorStrike(resourcePool.getBaseName("RawIron")),
    FlowTransferUpdater(resourcePool.getBaseName("Water")),
    FlowUpdater()
)

internal fun setUpParallelUpdate(map: WorldMap, resourcePool: ResourcePool, scale: Scale) {
    val maxSpeed = resourcePool.all.maxOf { it.genome.behaviour.tileSpeed(scale) }
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
    val tectonicPlates: MutableList<TectonicPlate> = ArrayList()
    val usedTiles: MutableSet<Tile> = HashSet()
    for (i in 0 until parameters.platesAmount) {
        val direction = randomElement(
            Direction.sides.asList(),
            random
        )
        val type = randomElement(
            TectonicPlate.Type.entries,
            random
        )
        val tectonicPlate = TectonicPlate(direction, type, parameters)
        val tile = randomTile(map)

        tectonicPlate.add(tile)
        tectonicPlates.add(tectonicPlate)
        usedTiles.add(tile)
    }
    var sw = true
    while (sw) {
        sw = false
        for (territory in tectonicPlates) {
            val brink = territory.filterOuterBrink { !usedTiles.contains(it) }

            if (brink.isEmpty())
                continue

            val tile = randomElement(brink, random)
            territory.add(tile)
            usedTiles.add(tile)

            sw = true
        }
    }
    return tectonicPlates
}

private fun fill(map: WorldMap) {
    var sw = true
    var ssw = true
    for (plate in map.tectonicPlates) {
        if (sw) {
            plate.type = TectonicPlate.Type.Terrain
            sw = false
        } else if (ssw) {
            plate.type = TectonicPlate.Type.Oceanic
            ssw = false
        }
        plate.initialize()
    }
    map.platesUpdate()
}
