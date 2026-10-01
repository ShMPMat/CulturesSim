package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.generator.setTileNeighbours
import io.tashtabash.sim.space.resource.Size
import io.tashtabash.sim.space.resource.createTestResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class WindCenterTest {
    // A land map with a lake in the middle, to create temperature differences
    private fun createMap(sizeX: Int, sizeY: Int): WorldMap {
        val map = WorldMap(List(sizeX) { x -> List(sizeY) { y -> createTestTile(x, y) } })
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

    private fun windOf(vararg levels: Pair<Tile, Double>) = Wind().apply {
        for ((tile, level) in levels)
            changeLevelOnTile(tile, level)
    }

    private fun Tile.movedAmount() = resourcesWithMoved.sumOf { it.amount }

    @Test
    fun `useWind splits a light resource between directions by level`() {
        val strongTarget = createTestTile(0, 0)
        val weakTarget = createTestTile(0, 1)
        val windCenter = WindCenter()
        windCenter.wind = windOf(strongTarget to data.maxWind / 2, weakTarget to data.maxWind / 4)
        val vapour = createTestResource(sizeRange = Size(.0001) to Size(.0001))
        vapour.addAmount(3000 - vapour.amount)

        windCenter.useWind(listOf(vapour))

        assertEquals(0, vapour.amount)
        assertEquals(2000, strongTarget.movedAmount())
        assertEquals(1000, weakTarget.movedAmount())
    }

    @Test
    fun `useWind blows away less of a heavier resource in weaker wind`() {
        fun blownAmount(level: Double): Int {
            val windCenter = WindCenter()
            windCenter.wind = windOf(createTestTile(0, 0) to level)
            val resource = createTestResource(sizeRange = Size(.05) to Size(.05))
            resource.addAmount(1_000_000 - resource.amount)

            windCenter.useWind(listOf(resource))
            return 1_000_000 - resource.amount
        }

        val weakWindAmount = blownAmount(data.maxWind / 10)
        val strongWindAmount = blownAmount(data.maxWind)

        assertTrue(weakWindAmount > 0, "Nothing is blown away")
        assertTrue(weakWindAmount < strongWindAmount, "$weakWindAmount >= $strongWindAmount")
    }
}
