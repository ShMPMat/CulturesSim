package io.tashtabash.sim.space

import io.tashtabash.sim.space.resource.createTestGenome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class ScaleTest {
    private val defaultScale = Scale(50.0, TEN_DAYS)

    @Test
    fun `toTileSpeed converts m per s into tiles per tick`() {
        // 1 m/s * 10 days of 86400 s, with the rest coefficient 25, over 50 km tiles
        assertEquals(10 * 86400 / 25.0 / 50_000, defaultScale.toTileSpeed(1.0), 1e-12)
        assertEquals(.0, defaultScale.toTileSpeed(.0))
    }

    @Test
    fun `toTileSpeed is inversely proportional to the tile size and proportional to the tick duration`() {
        val smallTiles = Scale(5.0, TEN_DAYS)
        val longTicks = Scale(50.0, 2 * TEN_DAYS)

        assertEquals(defaultScale.toTileSpeed(2.0) * 10, smallTiles.toTileSpeed(2.0), 1e-9)
        assertEquals(defaultScale.toTileSpeed(2.0) * 2, longTicks.toTileSpeed(2.0), 1e-9)
    }

    @Test
    fun `naturalDensity equals defaultAmount on the reference tile size`() {
        val genome = createTestGenome()

        assertEquals(genome.defaultAmount, genome.naturalDensity(defaultScale))
    }

    @Test
    fun `naturalDensity is proportional to the tile area and rounds up`() {
        val genome = createTestGenome() // defaultAmount = 10

        assertEquals(1, genome.naturalDensity(Scale(5.0, TEN_DAYS)))
        assertEquals(40, genome.naturalDensity(Scale(100.0, TEN_DAYS)))
    }
}

private const val TEN_DAYS = 10 * 24 * 60 * 60.0
