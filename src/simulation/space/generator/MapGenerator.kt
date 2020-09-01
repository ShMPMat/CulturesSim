package simulation.space.generator

import shmp.random.randomElement
import shmp.random.randomTile
import simulation.space.TectonicPlate
import simulation.space.WorldMap
import simulation.space.resource.Resource
import simulation.space.resource.ResourceType
import simulation.space.resource.container.ResourcePool
import simulation.space.resource.dependency.LabelerDependency
import simulation.space.tile.Tile
import simulation.space.tile.TypeUpdater
import java.util.*
import kotlin.random.Random

fun generateMap(x: Int, y: Int, platesAmount: Int, resourcePool: ResourcePool, random: Random): WorldMap {
    val tiles = createTiles(x, y, resourcePool.getBaseName("Water"))
    val map = WorldMap(tiles)
    setTileNeighbours(map)
    val tectonicPlates = randomPlates(
            platesAmount,
            map,
            random
    )
    tectonicPlates.forEach { map.addPlate(it) }
    fill(map)
    return map
}


fun fillResources(
        map: WorldMap,
        resourcePool: ResourcePool,
        supplement: MapGeneratorSupplement,
        random: Random
) {
    for (resource in resourcePool.getAll {
        it.genome.spreadProbability != 0.0
                || it.genome.type == ResourceType.Mineral
    }) {
        scatter(
                map,
                resourcePool,
                resource,
                random.nextInt(
                        supplement.startResourceAmountRange.first,
                        supplement.startResourceAmountRange.last
                ),
                random
        )
    }
}

private fun setTileNeighbours(map: WorldMap) {
    val x = map.maxX
    val y = map.maxY

    for (i in 0 until x)
        for (j in 0 until y)
            map[i, j]?.neighbours = arrayOf(
                    map[i, j + 1],
                    map[i, j - 1],
                    map[i + 1, j],
                    map[i - 1, j]
            ).filterNotNull()
}

private fun createTiles(x: Int, y: Int, water: Resource): List<List<Tile>> {
    val map: MutableList<List<Tile>> = ArrayList()
    (0 until x).map { i ->
        map.add(
                (0 until y).map { j -> Tile(i, j, TypeUpdater(water)) }
        )
    }
    return map
}

private fun randomPlates(platesAmount: Int, map: WorldMap, random: Random): List<TectonicPlate> {
    val tectonicPlates: MutableList<TectonicPlate> = ArrayList()
    val usedTiles: MutableSet<Tile> = HashSet()
    for (i in 0 until platesAmount) {
        val direction = randomElement(
                TectonicPlate.Direction.values().toList(),
                random
        )
        val type = randomElement(
                TectonicPlate.Type.values().toList(),
                random
        )
        val tectonicPlate = TectonicPlate(direction, type)
        val tile = randomTile(map, random)

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
            plate.setType(TectonicPlate.Type.Terrain)
            sw = false
        } else if (ssw) {
            plate.setType(TectonicPlate.Type.Oceanic)
            ssw = false
        }
        plate.initialize()
    }
    map.platesUpdate()
}

private fun scatter(map: WorldMap, resourcePool: ResourcePool, resource: Resource, n: Int, random: Random) {
    val attempts = 1000
    val goodTiles = map.getTiles { resource.isOptimal(it) }//TODO something wrong, it optimal and acceptable works inside-out

    for (i in 0 until n) {
        var tile: Tile
        if (goodTiles.isEmpty()) {
            tile = randomTile(map, random)
            var j = 0

            while (j < attempts && !resource.isAcceptable(tile)) {
                tile = randomTile(map, random)
                j++
            }
        } else
            tile = randomElement(goodTiles, random)

        tile.addDelayedResource(resource.copy())
        addDependencies(listOf(), resource, tile, resourcePool)
    }
}

private fun addDependencies(resourceStack: List<Resource>, resource: Resource, tile: Tile, resourcePool: ResourcePool) {
    val newStack = resourceStack + listOf(resource)
    for (dependency in resource.genome.dependencies) {
        if (!dependency.isPositive || !dependency.isResourceNeeded)
            continue
        if (dependency is LabelerDependency) {
            val suitableResources = resourcePool
                    .getAll { dependency.isResourceDependency(it) }
                    .filter { filterDependencyResources(it, newStack) }
            for (dependencyResource in suitableResources) {
                if (dependencyResource.isAcceptable(tile)) {
                    tile.addDelayedResource(dependencyResource)
                    addDependencies(newStack, dependencyResource, tile, resourcePool)
                }
            }
        }
    }
}

private fun filterDependencyResources(resource: Resource, previous: List<Resource>) =
        resource.genome.type in listOf(ResourceType.Plant, ResourceType.Animal)
                && previous.none { resource.simpleName == it.simpleName }
                && resource.genome.primaryMaterial != null
