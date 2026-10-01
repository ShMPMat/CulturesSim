package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.SpaceData.data
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class WindTest {
    @Test
    fun `changeLevelOnTile accumulates the level on an existing Tile`() {
        val wind = Wind()
        val tile = createTestTile(0, 0)

        wind.changeLevelOnTile(tile, 1.0)
        wind.changeLevelOnTile(tile, 2.0)

        assertEquals(3.0, wind.getLevelByTile(tile))
        assertEquals(1, wind.affectedTiles.size)
    }

    @Test
    fun `changeLevelOnTile removes a Tile when the level drops to zero`() {
        val wind = Wind()
        val tile = createTestTile(0, 0)

        wind.changeLevelOnTile(tile, 1.0)
        wind.changeLevelOnTile(tile, -1.0)

        assertTrue(wind.isStill)
        assertEquals(0.0, wind.getLevelByTile(tile))
    }

    @Test
    fun `changeLevelOnTile caps the level at maximalWind`() {
        val wind = Wind()
        val tile = createTestTile(0, 0)
        val otherTile = createTestTile(0, 1)

        wind.changeLevelOnTile(tile, data.maxWind * 2)
        wind.changeLevelOnTile(otherTile, data.maxWind - 1)
        wind.changeLevelOnTile(otherTile, 2.0)

        assertEquals(data.maxWind, wind.getLevelByTile(tile))
        assertEquals(data.maxWind, wind.getLevelByTile(otherTile))
    }

    @Test
    fun `changeLevelOnTile ignores non-positive changes on a new Tile`() {
        val wind = Wind()

        wind.changeLevelOnTile(createTestTile(0, 0), -1.0)
        wind.changeLevelOnTile(createTestTile(0, 1), 0.0)

        assertTrue(wind.isStill)
    }
}
