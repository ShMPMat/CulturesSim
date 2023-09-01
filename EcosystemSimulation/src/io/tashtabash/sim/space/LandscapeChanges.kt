package io.tashtabash.sim.space

import io.tashtabash.random.randomElement
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.Controller
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.TileTag
import io.tashtabash.sim.space.tile.getLakeTag
import io.tashtabash.sim.space.tile.getRiverTag
import java.lang.Integer.min
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
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
    val iterations = (200 * Controller.session.proportionCoefficient).toInt()
    loop@ for (i in 1..iterations) {
        if (currentTile.type == Tile.Type.Water || currentTile.resourcePack.contains(water)) {
            break
        }
        currentTile.addDelayedResource(water.copy((1000 * Controller.session.proportionCoefficient).toInt()))
        currentTile.tagPool.add(nameTag)
        val minLevel = min(
                currentTile.level,
                currentTile.getNeighbours(goodTilePredicate).minByOrNull { it.level }?.level ?: -1
        )
        val tiles = currentTile.getNeighbours { it.level == minLevel }
        if (minLevel == currentTile.level && tiles.size >= 3) {
            createLake(currentTile, water, goodTilePredicate, nameTag, random)
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
        previousRiverTag: TileTag,
        random: Random
) {
    val nameTag = getLakeTag(lakesCreated.toString())
    lakesCreated++
    val lakeTiles = mutableSetOf<Tile>()
    val outflowTiles = mutableSetOf<Tile>()
    val queue: Queue<Tile> = ArrayDeque()
    queue.add(tile)
    while (true) {
        val goodTiles = queue.poll()
                ?.getNeighbours(goodTilePredicate)
                ?: break
        val tiles = goodTiles.filter { it.level == tile.level }
        outflowTiles += goodTiles.filter { it.level < tile.level && tile.type != Tile.Type.Water }
        for (t in tiles)
            if (lakeTiles.add(t))
                queue += t
    }

    if (outflowTiles.isNotEmpty()) {
        val outflowNeighbours = outflowTiles.flatMap { it.neighbours }
        val crampedOutflowCount = outflowTiles.count { it in outflowNeighbours }
        val crampedOutflowPart = crampedOutflowCount.toDouble() / outflowTiles.size
        val riverProbability = crampedOutflowPart + (1 - crampedOutflowPart).pow(1.1)

        riverProbability.chanceOf {
            var path: List<Tile>? = null
            for (i in 1..10) {
                val endTile = outflowTiles.random(random)
                path = makeRiverPath(tile, endTile, lakeTiles + endTile, random)
                if (path != null)
                    break
            }
            path?.let { tiles ->
                tiles.forEach { it.addDelayedResource(water.copy((1000 * Controller.session.proportionCoefficient).toInt())) }
                tiles.forEach { it.tagPool.add(previousRiverTag) }
                createRiver(tiles.last(), water, goodTilePredicate, random)
                return
            }
        }
    }

    lakeTiles.forEach { it.addDelayedResource(water.copy((4000 * Controller.session.proportionCoefficient).toInt())) }
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



private fun makeRiverPath(start: Tile, finish: Tile, allowedTiles: Set<Tile>, random: Random): List<Tile>? {
    val h = {t: Tile -> abs(t.x - finish.x) + abs(t.y - finish.y) + random.nextInt(-2, 2) }
    val g = mutableMapOf<Tile, Int>()
    val f = mutableMapOf<Tile, Int>()
    val prev = mutableMapOf<Tile, Tile>()
    val q = PriorityQueue<Tile>(Comparator.comparingInt { t -> f.getValue(t) })
    val u = mutableSetOf<Tile>()

    g[start] = 0
    g[start] = h(start)
    q.add(start)

    var turns = 0
    while (q.isNotEmpty() && turns < (200 * Controller.session.proportionCoefficient).toInt()) {
        turns++

        val cur = q.remove()
        if (cur == finish)
            return unwind(start, finish, prev)

        u.add(cur)

        val neighbours = cur.neighbours.filter { it in allowedTiles }
        for (v in neighbours) {
            val distance = g.getValue(cur) + distance(cur, v)

            if (v in u && distance >= g.getValue(v))
                continue

            prev[v] = cur
            g[v] = distance
            f[v] = distance + h(v)
            q.add(v)
        }
    }

    return null
}

private fun unwind(start: Tile, cur: Tile, map: Map<Tile, Tile>): List<Tile> =
        (if (cur == start) emptyList() else unwind(start, map.getValue(cur), map)) + listOf(cur)

private fun distance(start: Tile, finish: Tile): Int {
    if (finish.resourcePack.any { it.simpleName == "Water" })
        return 0

    return abs(start.level - finish.level) + 1
}
