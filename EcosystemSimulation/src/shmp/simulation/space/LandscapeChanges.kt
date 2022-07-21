package shmp.simulation.space

import shmp.random.randomElement
import shmp.simulation.Controller
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.tile.Tile
import shmp.simulation.space.tile.getLakeTag
import shmp.simulation.space.tile.getRiverTag
import java.lang.Integer.min
import java.util.*
import kotlin.random.Random


private var riversCreated = 0
private var lakesCreated = 0

fun createRiver(
        tile: Tile,
        water: Resource,
        goodTilePredicate: (Tile) -> Boolean,
        random: Random
) {
    val nameTag = getRiverTag(riversCreated.toString())
    riversCreated++
    var currentTile = tile
    loop@ for (i in 0..200) {
        if (currentTile.type == Tile.Type.Water || currentTile.resourcePack.contains(water)) {
            break
        }
        currentTile.addDelayedResource(water.copy((2000 * Controller.session.proportionCoefficient).toInt()))
        currentTile.tagPool.add(nameTag)
        val minLevel = min(
                currentTile.level,
                currentTile.getNeighbours(goodTilePredicate).minByOrNull { it.level }?.level ?: -1
        )
        val tiles = currentTile.getNeighbours { it.level == minLevel }
        if (minLevel == currentTile.level && tiles.size >= 3) {
            createLake(currentTile, water, goodTilePredicate, random)
            break
        }
        currentTile = when (tiles.size) {
            0 -> break@loop
            else -> randomElement(tiles, random)
        }
    }
}

fun createLake(
        tile: Tile,
        water: Resource,
        goodTilePredicate: (Tile) -> Boolean,
        random: Random
) {
    val nameTag = getLakeTag(lakesCreated.toString())
    lakesCreated++
    val lakeTiles = mutableSetOf<Tile>()
    val outflowTiles = mutableSetOf<Tile>()
    val queue: Queue<Tile> = ArrayDeque()
    queue.add(tile)
    while (true) {
        val goodTiles = (queue.poll() ?: break).getNeighbours(goodTilePredicate)
        val tiles = goodTiles.filter { it.level == tile.level }
        outflowTiles.addAll(goodTiles.filter { it.level < tile.level && tile.type != Tile.Type.Water })
        tiles.forEach {
            if (lakeTiles.add(it)) {
                queue.add(it)
            }
        }
    }
    lakeTiles.forEach { it.addDelayedResource(water.copy(2)) }
    lakeTiles.forEach { it.tagPool.add(nameTag) }
    outflowTiles.forEach {
        createRiver(it, water, goodTilePredicate, random)
    }
}

fun createRivers(
        map: WorldMap,
        amount: Int,
        water: Resource,
        goodSpotProbability: (Tile) -> Double,
        goodTilePredicate: (Tile) -> Boolean,
        random: Random
) {
    val allTiles = map.tiles
            .map { it to goodSpotProbability(it) }
            .filter { it.second > 0.0 }
            .map { it.first }
    val actualAmount = min(amount, allTiles.size)
    if (actualAmount == 0)
        return

    repeat(actualAmount) {
        val tile = randomElement(allTiles, goodSpotProbability, random)
        createRiver(tile, water, goodTilePredicate, random)
    }
}
