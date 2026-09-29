package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.generator.setTileNeighbours
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class WindCenterTest {
    // A land map with a lake in the middle, to create temperature differences
    private fun createMap(sizeX: Int, sizeY: Int): WorldMap {
        val map = WorldMap(List(sizeX) { x -> List(sizeY) { y -> Tile(x, y) } })
        setTileNeighbours(map)
        for (x in sizeX / 3 until 2 * sizeX / 3)
            for (y in sizeY / 3 until 2 * sizeY / 3)
                map.getValue(x, y).setType(Tile.Type.Water, true)
        return map
    }

    private fun WorldMap.tick() {
        update()
        finishUpdate()
    }

    @Test
    fun `Wind doesn't grow to maximalWind over time`() {
        val map = createMap(15, 20)

        repeat(500) { map.tick() }
        val maxWind = map.tiles.maxOf { it.wind.maxLevel }

        assertTrue(maxWind < data.maxWind, "Wind reached maximalWind: $maxWind")
    }
}
