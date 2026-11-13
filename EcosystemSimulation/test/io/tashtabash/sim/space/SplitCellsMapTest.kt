package io.tashtabash.sim.space

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.createTestTile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random


// A map where some cells are split into a big land part and a small second part, like a condensed map
class SplitCellsMapTest {
    private val sizeX = 12
    private val sizeY = 14
    private val splitCells = listOf(3 to 4, 3 to 5, 8 to 0, 8 to 13)

    private val waterParts = mutableListOf<Tile>()
    private val map = createMap { it.setType(Tile.Type.Water, true) }

    private fun createMap(setUpPart: (Tile) -> Unit): WorldMap {
        waterParts.clear()
        val tiles = mutableListOf<Tile>()
        for (x in 0 until sizeX)
            for (y in 0 until sizeY) {
                val isSplit = x to y in splitCells
                val area = data.generation.scale.tileAreaKm2

                tiles += createTestTile(x, y, area = if (isSplit) area * .7 else area)
                if (isSplit)
                    waterParts += createTestTile(x, y, area = area * .3).also(setUpPart)
            }

        val map = WorldMap(tiles + waterParts, mockWorldExtent(sizeX, sizeY), data.generation.scale)
        // Tiles are neighbours if they are adjacent or share a cell
        for (tile in map.tiles)
            tile.setNeighbours(
                map.tiles.filter { it != tile }
                    .mapNotNull { other -> map.direction(tile, other)?.let { other to it } }
            )

        return map
    }

    @Test
    fun `tilesAt returns all parts of a cell`() {
        val (x, y) = splitCells[0]

        assertEquals(2, map.getTilesAt(x, y).size)
        assertTrue(waterParts[0] in map.getTilesAt(x, y))
        assertEquals(1, map.getTilesAt(0, 0).size)
    }

    @Test
    fun `tilesAt wraps on the looping axis`() {
        assertEquals(map.getTilesAt(8, 0), map.getTilesAt(8, sizeY))
        assertEquals(listOf<Tile>(), map.getTilesAt(sizeX, 0))
    }

    @Test
    fun `get refuses cells with several Tiles`() {
        val (x, y) = splitCells[0]

        assertThrows<IllegalStateException> { map[x, y] }
        assertNotNull(map[0, 0])
    }

    @Test
    fun `dominantTileAt returns the largest part`() {
        val (x, y) = splitCells[0]

        assertEquals(Tile.Type.Normal, map.getMainTile(x, y)?.type)
    }

    @Test
    fun `all parts are in tiles and lines`() {
        assertEquals(sizeX * sizeY + waterParts.size, map.tiles.size)
        assertEquals(map.tiles.toSet(), map.lines.flatten().toSet())
        assertTrue(map.lines[3].containsAll(waterParts.filter { it.x == 3 }))
    }

    @Test
    fun `parts of a cell are neighbours lying Here`() {
        val (x, y) = splitCells[0]
        val (land, water) = map.getTilesAt(x, y)

        assertEquals(Direction.Here, land.directionOf(water))
        // Four adjacent cells, one of them split, and the own water part
        assertEquals(6, land.neighbours.size)
    }

    @Test
    fun `calculateTileUpdateOrder puts parts of a cell into the same batch`() {
        for (group in map.calculateTileUpdateOrder(2))
            for (batch in group)
                for ((x, y) in splitCells)
                    if (map.getTilesAt(x, y).any { it in batch })
                        assertTrue(batch.containsAll(map.getTilesAt(x, y)))
    }

    @Test
    fun `calculateTileUpdateOrder batches in one group don't reach each other`() {
        val margin = 2

        for (group in map.calculateTileUpdateOrder(margin)) {
            val reachedBy = mutableMapOf<Tile, Int>()

            for ((i, batch) in group.withIndex())
                for (tile in batch.flatMap { it.getTilesInRadius(margin - 1) + it }) {
                    val other = reachedBy.put(tile, i)
                    assertTrue(other == null || other == i) {
                        "Tile ${tile.id} at ${tile.posStr} is reached by batches $other and $i"
                    }
                }
        }
    }

    @Test
    fun `map with split cells is updated`() {
        if (RandomSingleton.safeRandom == null)
            RandomSingleton.safeRandom = Random(1)

        repeat(3) {
            map.update()
            map.finishUpdate()
        }
        map.tileUpdateOrder = map.calculateTileUpdateOrder(2)
        repeat(3) {
            map.update()
            map.finishUpdate()
        }
    }

    @Test
    fun `map with split cells is eroded`() {
        // Highland parts, so that erosion doesn't sink Tiles below the sea level, which needs SaltWater
        val highlandMap = createMap { it.setLevel(106) }

        repeat(3) { highlandMap.geologicUpdate() }

        for ((x, y) in splitCells) {
            val part = highlandMap.getTilesAt(x, y)[1]
            assertTrue(part.level < 106, "Part at $x $y wasn't eroded")
            assertTrue(part.neighbours.any { it.level > 100 }, "No neighbour of $x $y got the eroded material")
        }
    }

    @Test
    fun `WorldMap rejects Tiles outside the map`() {
        assertThrows<IllegalArgumentException> {
            WorldMap(listOf(createTestTile(0, 0), createTestTile(1, 0)), mockWorldExtent(1, 1), data.generation.scale)
        }
    }

    @Test
    fun `WorldMap rejects empty cells`() {
        assertThrows<IllegalArgumentException> {
            WorldMap(listOf(createTestTile(0, 0)), mockWorldExtent(1, 2), data.generation.scale)
        }
    }
}
