package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.WorldMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class TileTest {
    private val scale = Scale(50.0, 10 * 24 * 60 * 60.0)

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
            WorldMap(listOf(listOf(Tile(1, 0, 0, scale = scale), Tile(1, 0, 1, scale = scale))))
        }
    }
}
