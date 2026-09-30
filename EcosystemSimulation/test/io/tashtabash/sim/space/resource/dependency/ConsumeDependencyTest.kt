package io.tashtabash.sim.space.resource.dependency

import io.tashtabash.sim.space.resource.OwnershipMarker
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.resource.createTestResource
import io.tashtabash.sim.space.resource.tag.labeler.BaseNameLabeler
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.tile.Tile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ConsumeDependencyTest {
    private val dependency = ConsumeDependency(1.0, true, QuantifiedResourceLabeler(BaseNameLabeler("Food"), 1.0))

    // The consumer has amount 10, so it needs 10 food per call; Tiles have no food
    private fun createConsumer(storedFood: Int): Resource =
        createTestResource(name = "Animal").apply { setConsumeBuffer(dependency, storedFood) }

    @Test
    fun `safe satisfaction doesn't spend the stored food`() {
        val consumer = createConsumer(25)

        val result = dependency.satisfaction(Tile(0, 0), consumer, true)

        assertEquals(1.0, result)
        assertEquals(25, consumer.getConsumeBuffer(dependency))
    }

    @Test
    fun `safe satisfaction doesn't change partially stored food`() {
        val consumer = createConsumer(4)

        val result = dependency.satisfaction(Tile(0, 0), consumer, true)

        assertEquals(0.4, result)
        assertEquals(4, consumer.getConsumeBuffer(dependency))
    }

    @Test
    fun `satisfied call spends the needed food and keeps the surplus`() {
        val consumer = createConsumer(25)

        val result = dependency.satisfaction(Tile(0, 0), consumer, false)

        assertEquals(1.0, result)
        assertEquals(15, consumer.getConsumeBuffer(dependency))
    }

    @Test
    fun `partially satisfied call spends the food instead of counting it again`() {
        val consumer = createConsumer(4)
        val tile = Tile(0, 0)

        val first = dependency.satisfaction(tile, consumer, false)
        val second = dependency.satisfaction(tile, consumer, false)

        assertEquals(0.4, first)
        assertEquals(0, consumer.getConsumeBuffer(dependency))
        assertEquals(0.0, second)
    }

    @Test
    fun `Resources of the same genome don't share stored food`() {
        val fed = createConsumer(25)
        val hungry = createConsumer(0)

        dependency.satisfaction(Tile(0, 0), fed, false)
        val result = dependency.satisfaction(Tile(1, 0), hungry, false)

        assertEquals(0.0, result)
        assertEquals(15, fed.getConsumeBuffer(dependency))
        assertEquals(0, hungry.getConsumeBuffer(dependency))
    }

    @Test
    fun `stored food is kept per dependency`() {
        val otherDependency = ConsumeDependency(1.0, true, QuantifiedResourceLabeler(BaseNameLabeler("Water"), 1.0))
        val consumer = createConsumer(25)

        otherDependency.satisfaction(Tile(0, 0), consumer, false)

        assertEquals(25, consumer.getConsumeBuffer(dependency))
        assertEquals(0, consumer.getConsumeBuffer(otherDependency))
    }

    @Test
    fun `merge adds up stored food`() {
        val consumer = createConsumer(25)
        val incoming = createConsumer(7)

        consumer.merge(incoming)

        assertEquals(32, consumer.getConsumeBuffer(dependency))
        assertEquals(0, incoming.getConsumeBuffer(dependency))
    }

    @Test
    fun `copies and split-off parts start without stored food`() {
        val consumer = createConsumer(25)

        val copy = consumer.copy(5)
        val part = consumer.getCleanPart(5, Taker.SeparationTaker)

        assertEquals(0, copy.getConsumeBuffer(dependency))
        assertEquals(0, part.getConsumeBuffer(dependency))
        assertEquals(25, consumer.getConsumeBuffer(dependency))
    }

    @Test
    fun `death and being eaten keep stored food`() {
        val consumer = createConsumer(25)

        consumer.getCleanPartInt(3, Taker.DeathTaker)
        consumer.getCleanPartInt(3, Taker.ResourceTaker(createTestResource(name = "Predator")))

        assertEquals(25, consumer.getConsumeBuffer(dependency))
    }

    @Test
    fun `swapOwnership keeps stored food`() {
        val consumer = createConsumer(25)

        val swapped = consumer.swapOwnership(OwnershipMarker("Owner"))

        assertEquals(25, swapped.getConsumeBuffer(dependency))
        assertEquals(0, consumer.getConsumeBuffer(dependency))
    }
}
