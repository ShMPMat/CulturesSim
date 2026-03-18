package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.ResourceCore
import io.tashtabash.sim.space.resource.createTestGenome
import io.tashtabash.sim.space.tile.Tile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class TemperatureMinTest {
    @Test
    fun `hasNeeded returns true if the temperature is higher than the threshold`() {
        val tile = Tile(0, 0)
        val resource = ResourceCore(createTestGenome()).fullCopy()
        val temperatureMin = TemperatureMin(tile.temperature.toInt() - 10, 0.5)

        assertTrue(temperatureMin.hasNeeded(tile))
        assert(temperatureMin.satisfaction(tile, resource, true) == 1.0)
    }

    @Test
    fun `hasNeeded returns false if the temperature is lower than the threshold`() {
        val tile = Tile(0, 0)
        val resource = ResourceCore(createTestGenome()).fullCopy()
        val temperatureMin = TemperatureMin(tile.temperature.toInt() + 2, 0.5)

        assertFalse(temperatureMin.hasNeeded(tile))
        assert(temperatureMin.satisfaction(tile, resource, true) < 1.0)
    }
}
