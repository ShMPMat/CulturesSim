package io.tashtabash.sim.space.generator

import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.assertValidTopology
import io.tashtabash.sim.space.createTestMap
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.createTestResource
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class MapCondenserTest {
    private val sizeX = 4
    private val sizeY = 6
    private val factor = 5
    private val fineScale = Scale(data.generation.scale.tileSizeKm / factor, data.generation.scale.tickDurationSeconds)
    private val cellArea = data.generation.scale.tileAreaKm2


    private val fine = createTestMap(sizeX * factor, sizeY * factor, scale = fineScale)

    // The fine Tiles of a coarse cell, row by row
    private fun cellTiles(x: Int, y: Int) = (x * factor until (x + 1) * factor).flatMap { fineX ->
        (y * factor until (y + 1) * factor).map { fineY -> fine.getValue(fineX, fineY) }
    }

    private fun makeWater(tiles: List<Tile>) = tiles.forEach { it.setType(Tile.Type.Water, true) }

    private fun condense(fine: WorldMap = this.fine, factor: Int = this.factor) =
        condenseMap(fine, factor, { mutableListOf() }, sliverThreshold = .1)

    @Test
    fun `condensing with factor 1 reproduces the map`() {
        val map = createTestMap(sizeX, sizeY)
        makeWater(listOf(map.getValue(1, 2), map.getValue(1, 3)))
        map.getValue(2, 4).setLevel(115)

        val condensed = condense(map, 1)

        for (tile in map.tiles) {
            val part = condensed.getValue(tile.x, tile.y)
            assertEquals(tile.type, part.type)
            assertEquals(tile.level, part.level)
            assertEquals(tile.secondLevel, part.secondLevel)
            assertEquals(tile.area, part.area)
            assertEquals(tile.neighbours.map { it.x to it.y }, part.neighbours.map { it.x to it.y })
        }
    }

    @Test
    fun `mixed cell is split into a land and a water part`() {
        val tiles = cellTiles(1, 1)
        makeWater(tiles.take(10))
        tiles.drop(10).take(5).forEach { it.setLevel(115) }

        val (land, water) = condense().tilesAt(1, 1)

        assertEquals(Tile.Type.Normal, land.type)
        assertEquals((10 * 100 + 5 * 115) / 15, land.level)
        assertEquals(cellArea * 15 / 25, land.area, 1e-9)
        assertEquals(Tile.Type.Water, water.type)
        assertEquals(data.seabedLevel, water.level)
        assertEquals(cellArea * 10 / 25, water.area, 1e-9)
    }

    @Test
    fun `land part is a mountain if it's high on average`() {
        val tiles = cellTiles(2, 3)
        tiles.take(20).forEach { it.setLevel(115) }

        val part = condense().getValue(2, 3)

        assertEquals(Tile.Type.Mountain, part.type)
        assertEquals((20 * 115 + 5 * 100) / 25, part.level)
    }

    @Test
    fun `sliver doesn't make a part`() {
        makeWater(cellTiles(1, 1).take(2))
        makeWater(cellTiles(2, 2).take(24))

        val map = condense()
        val land = map.getValue(1, 1)
        val water = map.getValue(2, 2)

        assertEquals(Tile.Type.Normal, land.type)
        assertEquals(99, land.level)
        assertEquals(cellArea, land.area, 1e-9)
        assertEquals(Tile.Type.Water, water.type)
        assertEquals(cellArea, water.area, 1e-9)
    }

    @Test
    fun `cell of one kind makes a single part without a sliver threshold`() {
        makeWater(cellTiles(1, 1).take(1))

        val map = condenseMap(fine, factor, { mutableListOf() }, sliverThreshold = .0)

        assertEquals(1, map.tilesAt(0, 0).size)
        assertEquals(2, map.tilesAt(1, 1).size)
        assertEquals(cellArea / 25, map.tilesAt(1, 1)[1].area, 1e-9)
    }

    @Test
    fun `parts of every cell cover the cell`() {
        makeWater(cellTiles(1, 1).take(10))
        makeWater(cellTiles(0, 0).take(2))

        val map = condense()

        for (x in 0 until sizeX)
            for (y in 0 until sizeY)
                assertEquals(cellArea, map.tilesAt(x, y).sumOf { it.area }, 1e-9)
    }

    @Test
    fun `parts neighbour all parts of adjacent cells and each other`() {
        makeWater(cellTiles(1, 1).take(10))
        makeWater(cellTiles(0, sizeY - 1).take(10))

        val map = condense()
        assertValidTopology(map)

        val (land, water) = map.tilesAt(1, 1)
        assertEquals(Direction.Here, land.directionOf(water))
        assertEquals(5, land.neighbours.size)
        val adjacent = map.getValue(1, 2)
        assertEquals(Direction.YMinus, adjacent.directionOf(land))
        assertEquals(Direction.YMinus, adjacent.directionOf(water))
        assertEquals(5, adjacent.neighbours.size)

        // Across the looping axis
        val acrossSeam = map.getValue(0, 0)
        assertTrue(acrossSeam.neighbours.containsAll(map.tilesAt(0, sizeY - 1)))
    }

    @Test
    fun `ids follow the cells, land before water`() {
        makeWater(cellTiles(1, 1).take(10))

        val map = condense()

        assertEquals(map.tiles.indices.toList(), map.tiles.map { it.id })
        val (land, water) = map.tilesAt(1, 1)
        assertTrue(land.id < water.id)
        assertEquals(Tile.Type.Water, water.type)
    }

    private val salt = createTestResource("Salt")
    private val plant = createTestResource("Plant")

    private fun Tile.amountOf(resource: Resource) =
        resourcesWithMoved.filter { it.baseName == resource.baseName }.sumOf { it.amount }

    @Test
    fun `resources of the fine Tiles are moved to their parts`() {
        val tiles = cellTiles(1, 1)
        makeWater(tiles.take(10))
        tiles.take(10).forEach { it.addDelayedResource(salt.copy(5)) }
        tiles.drop(10).forEach { it.addDelayedResource(plant.copy(3)) }

        val (land, water) = condense().tilesAt(1, 1)

        assertEquals(50, water.amountOf(salt))
        assertEquals(0, water.amountOf(plant))
        assertEquals(45, land.amountOf(plant))
        assertEquals(0, land.amountOf(salt))
    }

    @Test
    fun `equal resources merge in the part`() {
        val tiles = cellTiles(1, 1)
        tiles.forEach { it.addDelayedResource(plant.copy(3)) }
        tiles.forEach { it.middleUpdate() } // Some resources are in the pack, some are delayed
        tiles.take(5).forEach { it.addDelayedResource(plant.copy(1)) }

        val part = condense().getValue(1, 1)
        part.middleUpdate()

        assertEquals(listOf(25 * 3 + 5), part.resourcePack.resources.map { it.amount })
    }

    @Test
    fun `resources of a sliver go to the single part`() {
        val sliver = cellTiles(2, 2).take(2)
        makeWater(sliver)
        sliver.forEach { it.addDelayedResource(salt.copy(5)) }

        val part = condense().getValue(2, 2)

        assertEquals(Tile.Type.Normal, part.type)
        assertEquals(10, part.amountOf(salt))
    }

    @Test
    fun `every resource goes to exactly one part`() {
        makeWater(cellTiles(1, 1).take(10))
        makeWater(cellTiles(2, 2).take(2))
        for ((i, tile) in fine.tiles.withIndex())
            tile.addDelayedResource(plant.copy(i % 4 + 1))
        val fineAmount = fine.tiles.sumOf { it.amountOf(plant) }

        val map = condense()

        assertEquals(fineAmount, map.tiles.sumOf { it.amountOf(plant) })
    }

    @Test
    fun `condensed cells are factor times larger`() {
        val map = condense()

        assertEquals(sizeX, map.maxX)
        assertEquals(sizeY, map.maxY)
        assertEquals(data.generation.scale.tileSizeKm, map.getValue(0, 0).scale.tileSizeKm, 1e-9)
    }

    private fun condenseRegion(region: Region, moveResources: Boolean = false) =
        condenseRegion(fine, factor, region, { mutableListOf() }, .1, moveResources)

    // What identifies a part apart from its id
    private fun Tile.description() = listOf(x, y, type, level, secondLevel, area, prettyTemperature)

    @Test
    fun `region gives the same parts as the whole map`() {
        makeWater(cellTiles(1, 1).take(10))
        makeWater(cellTiles(2, 2).take(2))
        val region = Region(1, 1, 2, 3)

        val parts = condenseRegion(region).tiles
        val whole = condense()

        val expected = (1..2).flatMap { x -> (1..3).flatMap { y -> whole.tilesAt(x, y) } }
        assertEquals(expected.map { it.description() }, parts.map { it.description() })
    }

    @Test
    fun `region parts neighbour only parts inside the region`() {
        makeWater(cellTiles(1, 1).take(10))
        val region = Region(1, 1, 2, 3)

        val parts = condenseRegion(region).tiles
        val whole = condense()

        for (part in parts) {
            assertTrue(part.neighbours.all { it in parts })
            for (neighbour in part.neighbours)
                assertEquals(part.directionOf(neighbour)?.opposite, neighbour.directionOf(part))

            val wholePart = whole.tilesAt(part.x, part.y).first { it.description() == part.description() }
            val expectedNeighbours = wholePart.neighbours
                .filter { it.x in 1..2 && it.y in 1..3 }
                .map { it.description() }
            assertEquals(expectedNeighbours, part.neighbours.map { it.description() })
        }
    }

    @Test
    fun `region links across the looping axis`() {
        val parts = condenseRegion(Region(0, 0, 1, sizeY)).tiles

        val first = parts.first { it.y == 0 }
        val last = parts.first { it.y == sizeY - 1 }
        assertEquals(Direction.YMinus, first.directionOf(last))
    }

    @Test
    fun `region can cross the edge of the looping axis`() {
        makeWater(cellTiles(1, 0).take(10))

        val map = condenseRegion(Region(1, sizeY - 1, 2, 2))
        val whole = condense()

        assertEquals(listOf(sizeY - 1, 0), map.yCoordinates)
        assertValidTopology(map)
        for (part in map.tiles)
            assertTrue(whole.tilesAt(part.x, part.y).any { it.description() == part.description() })
        assertEquals(Direction.YPlus, map.getValue(2, sizeY - 1).directionOf(map.getValue(2, 0)))
    }

    @Test
    fun `region doesn't take resources unless asked`() {
        val tiles = cellTiles(1, 1)
        tiles.forEach { it.addDelayedResource(plant.copy(3)) }

        val parts = condenseRegion(Region(1, 1, 1, 1)).tiles

        assertEquals(0, parts.sumOf { it.amountOf(plant) })
        assertEquals(25 * 3, tiles.sumOf { it.amountOf(plant) })
    }

    @Test
    fun `region must be inside the condensed map`() {
        assertThrows<IllegalArgumentException> { condenseRegion(Region(sizeX - 1, 0, 2, 1)) }
        assertThrows<IllegalArgumentException> { condenseRegion(Region(-1, 0, 1, 1)) }
    }

    @Test
    fun `map sizes must be divisible by the factor`() {
        assertThrows<IllegalArgumentException> { condense(factor = 3) }
    }
}
