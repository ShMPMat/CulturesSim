package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.space.Data
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.sim.space.resource.OwnershipMarker
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.material.MaterialPool
import io.tashtabash.sim.space.tile.Tile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class PopulationCenterTest {
    init {
        val field = SpaceData::class.java.getDeclaredField("wasCalled") // Clean up just in case
        field.isAccessible = true
        field.set(SpaceData, false)
        SpaceData.data = Data(
            materialPool = MaterialPool(listOf(Material("Meat", 1.0, listOf()))),
        )
    }

    private var mockTile = Tile(0, 0, mutableListOf())
    private var mockMarker = OwnershipMarker("test")
    private var mockResources = ResourcePack()

    @Test
    fun `decreasePopulation correctly reduces total amount and handles underflow`() {
        val center = PopulationCenter(
            50,
            10,
            1,
            mockMarker,
            mockTile,
            mockResources,
            listOf()
        )

        center.decreasePopulation(20)
        assertEquals(30, center.amount)

        center.decreasePopulation(100)
        assertEquals(0, center.amount, "Population should not drop below zero")
    }
}
