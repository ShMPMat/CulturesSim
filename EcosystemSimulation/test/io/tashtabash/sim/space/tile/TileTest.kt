package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.createTestMap
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.mockWorldExtent
import io.tashtabash.sim.space.resource.createTestResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs


class TileTest {
    private val scale = Scale(50.0, 10 * 24 * 60 * 60)

    @Test
    fun `area defaults to the Scale tile area`() {
        assertEquals(2500.0, Tile(0, 0, 0, scale = scale).area)
    }

    @Test
    fun `area can differ from the Scale tile area`() {
        val tile = Tile(0, 0, 0, scale = scale, area = 100.0)

        assertEquals(100.0, tile.area)
        assertEquals(50.0, tile.scale.tileSizeKm)
    }

    @Test
    fun `getTilesInRadius works for radii which aren't cached`() {
        val map = createTestMap(30, 30)
        val tile = map.getValue(15, 15)

        // Within 12 steps on a grid: a diamond of 2 * 12 * 13 Tiles
        val expected = map.tiles.filter { it != tile && abs(it.x - 15) + abs(it.y - 15) <= 12 }
        assertEquals(2 * 12 * 13, expected.size)
        assertEquals(expected.toSet(), tile.getTilesInRadius(12).toSet())
    }

    @Test
    fun `Tiles at the same coordinates with different ids are different`() {
        val land = Tile(1, 0, 0, scale = scale)
        val water = Tile(2, 0, 0, scale = scale)

        assertNotEquals(land, water)
        assertEquals(2, setOf(land, water).size)
    }

    @Test
    fun `Tiles with the same id are equal`() {
        assertEquals(Tile(1, 0, 0, scale = scale), Tile(1, 5, 7, scale = scale))
        assertEquals(Tile(1, 0, 0, scale = scale).hashCode(), Tile(1, 5, 7, scale = scale).hashCode())
    }

    @Test
    fun `WorldMap rejects duplicate Tile ids`() {
        assertThrows<IllegalArgumentException> {
            WorldMap(listOf(Tile(1, 0, 0, scale = scale), Tile(1, 0, 1, scale = scale)), mockWorldExtent(1, 2), data.generation.scale)
        }
    }

    @Test
    fun `addDelayedResources result in the same outcome`() {
        val from = Tile(0, 0, 0, scale = scale)
        val to = Tile(1, 1, 0, scale = scale)
        val melon = createTestResource("Melon")
        val watermelon = createTestResource("Watermelon")
        from.addDelayedResource(melon.copy(100))
        from.flushDelayedResources()
        assertEquals(100, from.resourcePack.getAmount(melon))

        from.addDelayedResource(melon.copy(100))
        from.flushDelayedResources()
        assertEquals(200, from.resourcePack.getAmount(melon))

        from.addDelayedResource(melon, 100)
        from.flushDelayedResources()
        assertEquals(300, from.resourcePack.getAmount(melon))

        from.addDelayedResource(watermelon, 100)
        from.flushDelayedResources()
        assertEquals(100, from.resourcePack.getAmount(watermelon))

        from.addDelayedResource(watermelon, 100)
        from.addDelayedResource(watermelon, 100)
        from.flushDelayedResources()
        assertEquals(300, from.resourcePack.getAmount(watermelon))

        to.addDelayedResource(from.resourcePack.getUnpackedResource(melon).getCleanPart(50, null))
        to.addDelayedResource(from.resourcePack.getUnpackedResource(watermelon), from.resourcePack.getUnpackedResource(watermelon).getCleanPartInt(50, null))
        to.flushDelayedResources()
        assertEquals(250, from.resourcePack.getAmount(watermelon))
        assertEquals(250, from.resourcePack.getAmount(melon))
        assertEquals(50, to.resourcePack.getAmount(melon))
        assertEquals(50, to.resourcePack.getAmount(watermelon))
    }
}
