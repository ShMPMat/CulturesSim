package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.Scale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TileTest {
    private val scale = Scale(50.0, 10 * 24 * 60 * 60.0)

    @Test
    fun `area defaults to the Scale tile area`() {
        assertEquals(2500.0, Tile(0, 0, scale = scale).area)
    }

    @Test
    fun `area can differ from the Scale tile area`() {
        val tile = Tile(0, 0, scale = scale, area = 100.0)

        assertEquals(100.0, tile.area)
        assertEquals(50.0, tile.scale.tileSizeKm)
    }
}
