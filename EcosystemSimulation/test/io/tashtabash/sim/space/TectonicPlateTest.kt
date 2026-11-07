package io.tashtabash.sim.space

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.tile.Direction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.random.Random


// Relies on the default map looping: x isn't looping, y is
class TectonicPlateTest {
    private val sizeX = 3
    private val sizeY = 6
    private val map = createTestMap(sizeX, sizeY)
    private val parameters = data.generation

    // A plate on the column y = 0; the rest of the map is a still plate
    private fun createPlates(direction: Direction): TectonicPlate {
        if (RandomSingleton.safeRandom == null)
            RandomSingleton.safeRandom = Random(1)

        val plate = TectonicPlate(direction, TectonicPlate.Type.Terrain, parameters, 0)
        val rest = TectonicPlate(direction, TectonicPlate.Type.Terrain, parameters, 1)
        for (tile in map.tiles)
            if (tile.y == 0)
                plate.add(tile)
            else
                rest.add(tile)

        return plate
    }

    @Test
    fun `plate affects the Tiles in front of it`() {
        val plate = createPlates(Direction.YPlus)

        assertEquals(
            map.tiles.filter { it.y == 1 }.toSet(),
            plate.affectedTiles.map { it.first }.toSet()
        )
    }

    @Test
    fun `plate affects the Tiles in front of it across the looping axis`() {
        val plate = createPlates(Direction.YMinus)

        assertEquals(
            map.tiles.filter { it.y == sizeY - 1 }.toSet(),
            plate.affectedTiles.map { it.first }.toSet()
        )
    }

    @Test
    fun `plate can move to Here`() {
        assertDoesNotThrow {
            TectonicPlate(Direction.Here, TectonicPlate.Type.Terrain, parameters, 0)
        }
    }
}
