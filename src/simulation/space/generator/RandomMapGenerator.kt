package simulation.space.generator

import shmp.random.randomElement
import shmp.random.randomTile
import simulation.space.ResourcePool
import simulation.space.TectonicPlate
import simulation.space.Tile
import simulation.space.WorldMap
import simulation.space.resource.Genome
import simulation.space.resource.Resource
import simulation.space.resource.dependency.ResourceNeedDependency
import java.util.*
import kotlin.random.Random

fun generateMap(x: Int, y: Int, platesAmount: Int, random: Random): WorldMap {
    val tiles = createTiles(x, y)
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
    for (resource in resourcePool.getResourcesWithPredicate {
        it.spreadProbability != 0.0
                || it.genome.type == Genome.Type.Mineral
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
    val x = map.x
    val y = map.y
    for (i in 0 until x) {
        for (j in 0 until y) {
            map[i, j].neighbours = arrayOf(
                    map[i, j + 1],
                    map[i, j - 1],
                    map[i + 1, j],
                    map[i - 1, j]
            ).filter { Objects.nonNull(it) }
        }
    }
}

private fun createTiles(x: Int, y: Int): List<MutableList<Tile>> {
    val map: MutableList<MutableList<Tile>> = ArrayList()
    (0 until x).map { i ->
        map.add(
                (0 until y).map { j -> Tile(i, j) }.toMutableList()
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
            val brink = territory.getBrinkWithCondition { !usedTiles.contains(it) }
            if (brink.isEmpty()) {
                continue
            }
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
    val goodTiles = map.getTilesWithPredicate { resource.genome.isOptimal(it) }
    for (i in 0 until n) {
        var tile: Tile
        if (goodTiles.isEmpty()) {
            tile = randomTile(map, random)
            while (!resource.genome.isAcceptable(tile)) {
                tile = randomTile(map, random)
            }
        } else {
            tile = randomElement(goodTiles, random)
        }
        tile.addDelayedResource(resource.copy())
        addDependencies(resource, tile, resourcePool)
    }
}

private fun addDependencies(resource: Resource, tile: Tile, resourcePool: ResourcePool) {
    for (dependency in resource.genome.dependencies) {
        if (!dependency.isPositive || !dependency.isResourceNeeded) {
            continue
        }
        if (dependency is ResourceNeedDependency && dependency.resourceNames.any { it == "Vapour" }) {
            return
        }
        if (dependency is ResourceNeedDependency) {
            for (name in dependency.resourceNames) {
                val dep = resourcePool.getResource(name)
                if (dep.genome.isAcceptable(tile)) {
                    tile.addDelayedResource(dep)
                }
                addDependencies(dep, tile, resourcePool)
            }
            for (name in dependency.materialNames) {
                for (dep in resourcePool.getResourcesWithPredicate {
                    it.spreadProbability > 0
                            && it.simpleName != resource.simpleName
                            && it.genome.primaryMaterial != null
                            && it.genome.primaryMaterial.name == name
                }) {
                    if (dep.genome.isAcceptable(tile)) {
                        tile.addDelayedResource(dep.copy())
                        addDependencies(dep, tile, resourcePool)
                    }
                }
            }
        }
    }
}
