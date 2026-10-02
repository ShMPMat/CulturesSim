package io.tashtabash.sim.space.generator

import io.tashtabash.random.randomElement
import io.tashtabash.random.randomTile
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
import io.tashtabash.sim.space.tile.updater.TypeUpdater
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random


fun generateMap(
    x: Int,
    y: Int,
    platesAmount: Int,
    resourcePool: ResourcePool,
    random: Random,
    scale: Scale = data.defaultScale
): WorldMap {
    val tiles = createTiles(x, y, resourcePool, scale)
    val map = WorldMap(tiles)
    val flowTransferUpdater = FlowTransferUpdater(resourcePool.getBaseName("Water"))
    for (tile in tiles.flatten())
        tile.updaters += listOf(
            flowTransferUpdater,
            FlowUpdater()
        )
    setTileNeighbours(map)
    val tectonicPlates = randomPlates(
        platesAmount,
        map,
        random
    )
    tectonicPlates.forEach { map.addPlate(it) }
    fill(map)

    val maxSpeed = resourcePool.all.maxOf { it.genome.behaviour.tileSpeed(scale) }
    map.tileUpdateOrder = map.calculateTileUpdateOrder(ceil(maxSpeed + 1).toInt())

    return map
}

internal fun setTileNeighbours(map: WorldMap) {
    for (i in 0 until map.maxX)
        for (j in 0 until map.maxY)
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

private fun createTiles(x: Int, y: Int, resourcePool: ResourcePool, scale: Scale): List<List<Tile>> {
    val map: MutableList<List<Tile>> = ArrayList()
    val updaters = listOf(
        TypeUpdater(resourcePool.getBaseName("Water")),
        MeteorStrike(resourcePool.getBaseName("RawIron"))
    )

    for (i in 0 until x)
        map += (0 until y).map { j -> Tile(i * y + j, i, j, updaters.toMutableList(), scale) }

    return map
}

private fun randomPlates(platesAmount: Int, map: WorldMap, random: Random): List<TectonicPlate> {
    val tectonicPlates: MutableList<TectonicPlate> = ArrayList()
    val usedTiles: MutableSet<Tile> = HashSet()
    for (i in 0 until platesAmount) {
        val direction = randomElement(
            Direction.sides.asList(),
            random
        )
        val type = randomElement(
            TectonicPlate.Type.entries,
            random
        )
        val tectonicPlate = TectonicPlate(direction, type)
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
