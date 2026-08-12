package io.tashtabash.sim.space.resource

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.space.tile.Tile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class ResourceTest {
    @Test
    fun `getPart() never returns the whole amount on bigger amounts`() {
        if (RandomSingleton.safeRandom == null)
            RandomSingleton.safeRandom = Random(1)

        repeat(1000) {
            val resource = createTestResource()
            resource.addAmount(10_00)
            val amount = resource.amount
            assertTrue(resource.getPart(100_000, Taker.WindTaker).amount != amount)
        }

        val fastResource = createTestResource(behaviour = Behaviour(.0, .0, .0, 100.0, OverflowType.Cut))
        repeat(1000) {
            val resource = createTestResource()
            resource.addAmount(10_000)
            val amount = resource.amount
            assertTrue(resource.getPart(100_000, fastResource).amount != amount)
        }
    }

    @Test
    fun `merge of Resource with less deathTurn results in less deathPart`() {
        if (RandomSingleton.safeRandom == null)
            RandomSingleton.safeRandom = Random(1)

        val tile = Tile(0, 0)
        val oldResource = createTestResource(lifespan = 1.0)
        val newResource = createTestResource(lifespan = 1.0)
        oldResource.update(tile)
        oldResource.merge(newResource)
        oldResource.update(tile)
        assertTrue(oldResource.amount > 0)
    }
}
