package simulation.space

import shmp.random.randomElement
import simulation.space.resource.Resource
import simulation.space.tile.Tile
import java.lang.Integer.min
import java.util.*
import kotlin.random.Random

fun createRiver(
        tile: Tile,
        water: Resource,
        goodTilePredicate: (Tile) -> Boolean,
        random: Random
) {
    var currentTile = tile
    loop@ for (i in 0..200) {
        if (currentTile.type == Tile.Type.Water || currentTile.resourcePack.contains(water)) {
            break
        }
        currentTile.addDelayedResource(water.copy(2))
        val minLevel = min(
                currentTile.level,
                currentTile.getNeighbours(goodTilePredicate).minBy { it.level }?.level ?: -1
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
    val lakeTiles = mutableSetOf<Tile>()
    val outflowTiles = mutableSetOf<Tile>()
    val queue: Queue<Tile> = ArrayDeque()
    queue.add(tile)
    while (true) {
        val goodTiles = (queue.poll() ?: break).getNeighbours(goodTilePredicate)
        val tiles = goodTiles.filter { it.level == tile.level }
        outflowTiles.addAll(goodTiles.filter {it.level < tile.level})
        tiles.forEach {
            if (lakeTiles.add(it)) {
                queue.add(it)
            }
        }
    }
    lakeTiles.forEach { it.addDelayedResource(water.copy(2)) }
    outflowTiles.forEach {
        createRiver(it, water, goodTilePredicate, random)
    }
}

fun createRivers(
        map: WorldMap,
        amount: Int,
        water: Resource,
        goodSpotPredicate: (Tile) -> Boolean,
        goodTilePredicate: (Tile) -> Boolean,
        random: Random
) {
    val allTiles = map.getTiles(goodSpotPredicate)
    allTiles.shuffled(random).take(amount).forEach { createRiver(it, water, goodTilePredicate, random) }
}