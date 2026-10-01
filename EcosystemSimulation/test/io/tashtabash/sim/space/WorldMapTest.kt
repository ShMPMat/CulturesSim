package io.tashtabash.sim.space

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.space.generator.setTileNeighbours
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.createTestTile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random


class WorldMapTest {
    private fun createMap(sizeX: Int, sizeY: Int): WorldMap {
        val map = WorldMap(List(sizeX) { x -> List(sizeY) { y -> createTestTile(x, y) } })
        setTileNeighbours(map)
        return map
    }

    private fun tilesInRadius(tiles: Collection<Tile>, radius: Int): Set<Tile> {
        val result = tiles.toMutableSet()
        var front = tiles.toSet()
        repeat(radius) {
            front = front.flatMap { it.neighbours }
                .filter { it !in result }
                .toSet()
            result += front
        }
        return result
    }

    @ParameterizedTest
    @CsvSource("45, 60, 1", "45, 60, 2", "45, 60, 3", "45, 60, 5", "20, 26, 3", "7, 9, 2")
    fun `calculateTileUpdateOrder covers every Tile exactly once`(sizeX: Int, sizeY: Int, margin: Int) {
        val map = createMap(sizeX, sizeY)

        val orderedTiles = map.calculateTileUpdateOrder(margin).flatten().flatten()

        assertEquals(map.tiles.size, orderedTiles.size)
        assertEquals(map.tiles.toSet(), orderedTiles.toSet())
    }

    @ParameterizedTest
    @CsvSource("45, 60, 1", "45, 60, 2", "45, 60, 3", "45, 60, 5", "20, 26, 3", "7, 9, 2")
    fun `calculateTileUpdateOrder batches in one group don't reach each other`(sizeX: Int, sizeY: Int, margin: Int) {
        val map = createMap(sizeX, sizeY)
        val reach = margin - 1

        for (group in map.calculateTileUpdateOrder(margin)) {
            val reachedBy = mutableMapOf<Tile, Int>()

            for ((i, batch) in group.withIndex())
                for (tile in tilesInRadius(batch, reach)) {
                    val other = reachedBy.put(tile, i)
                    assertTrue(other == null) {
                        "Tile ${tile.x}, ${tile.y} is reached by batches $other and $i"
                    }
                }
        }
    }

    @Test
    fun `runOnMap gives every Tile the same random numbers on each run with the same seed`() {
        val map = createMap(45, 60)
        val order = map.calculateTileUpdateOrder(3)

        fun drawPerTile(seed: Int): Map<Tile, List<Int>> {
            RandomSingleton.safeRandom = Random(seed)
            val draws = ConcurrentHashMap<Tile, List<Int>>()
            map.runOnMap(order) { tile ->
                draws[tile] = List(5) { RandomSingleton.random.nextInt() }
            }
            return draws
        }

        val first = drawPerTile(42)
        val second = drawPerTile(42)

        assertEquals(map.tiles.size, first.size)
        assertEquals(first, second)
        assertNotEquals(first, drawPerTile(43))
    }

    @Test
    fun `runOnMap doesn't leave the batch Random bound after it finishes`() {
        val map = createMap(20, 26)
        val global = Random(1)
        RandomSingleton.safeRandom = global

        map.runOnMap(map.calculateTileUpdateOrder(3)) { RandomSingleton.random.nextInt() }

        assertSame(global, RandomSingleton.random)
    }

    @ParameterizedTest
    @CsvSource("45, 60, 1, 4", "45, 60, 3, 4", "7, 9, 2, 1")
    fun `calculateTileUpdateOrder parallelizes when the map is large enough`(
        sizeX: Int,
        sizeY: Int,
        margin: Int,
        minBatchesPerGroup: Int
    ) {
        val map = createMap(sizeX, sizeY)

        val order = map.calculateTileUpdateOrder(margin)

        for (group in order)
            assertTrue(group.size >= minBatchesPerGroup) { "Group has only ${group.size} batches" }
    }
}
