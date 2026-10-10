package io.tashtabash.sim.space.tile

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.resource.createTestResource
import io.tashtabash.sim.space.resource.dependency.NeedDependency
import io.tashtabash.sim.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random


class DependencyMatchesTest {
    private val foodLabeler = object : ResourceLabeler() {
        override fun isSuitable(genome: Genome) = genome.name.startsWith("Food")
    }
    private val dependency = NeedDependency(1.0, true, QuantifiedResourceLabeler(foodLabeler, 1.0))

    private val tile = createTestTile(0, 0)

    @BeforeEach
    fun setUp() {
        RandomSingleton.safeRandom = Random(1)
    }

    private fun addResources(vararg resources: Resource) {
        resources.forEach { tile.addDelayedResource(it) }
        tile.middleUpdate()
    }

    // What a full rebuild would return
    private fun expectedMatches() = tile.resourcePack.resources.filter { it.simpleName.startsWith("Food") }

    private fun DependencyMatches.toList(): List<Pair<Resource, Int>> {
        val result = mutableListOf<Pair<Resource, Int>>()
        any { resource, worth -> result += resource to worth; false }
        return result
    }

    // The order of the matches doesn't matter, but they must be the same objects as in the pack
    private fun assertMatchesUpToDate() {
        val matches = tile.getDependencyMatches(dependency).toList()
        val expected = expectedMatches()
        assertEquals(expected.size, matches.size)
        assertTrue(expected.all { e -> matches.count { (a, _) -> a === e } == 1 })
        assertTrue(matches.all { (_, worth) -> worth == 1 })
    }

    @Test
    fun `matches include new kinds of Resources`() {
        addResources(createTestResource("FoodB"))
        assertMatchesUpToDate()

        addResources(createTestResource("Grass"), createTestResource("FoodA"), createTestResource("FoodC"))

        assertMatchesUpToDate()
        assertEquals(
            setOf("FoodA", "FoodB", "FoodC"),
            tile.getDependencyMatches(dependency).toList().map { it.first.simpleName }.toSet()
        )
    }

    @Test
    fun `merging into an existing Resource keeps the matches`() {
        addResources(createTestResource("FoodA"))
        val before = tile.getDependencyMatches(dependency).toList().single().first

        addResources(createTestResource("FoodA"))

        assertSame(before, tile.getDependencyMatches(dependency).toList().single().first)
        assertMatchesUpToDate()
    }

    @Test
    fun `dead Resources are removed from the matches`() {
        addResources(createTestResource("FoodA"), createTestResource("FoodB"), createTestResource("Grass"))
        assertMatchesUpToDate()
        val foodA = tile.resourcePack.resources.first { it.simpleName == "FoodA" }

        foodA.getCleanPartInt(foodA.amount, Taker.DeathTaker)
        tile.startUpdate()

        assertFalse(tile.resourcePack.contains(foodA))
        assertMatchesUpToDate()
    }

    @Test
    fun `removeResource is reflected in the matches`() {
        addResources(createTestResource("FoodA"), createTestResource("FoodB"))
        assertMatchesUpToDate()

        tile.removeResource(tile.resourcePack.resources.first { it.simpleName == "FoodB" })

        assertMatchesUpToDate()
    }

    @Test
    fun `removeAll keeps the remaining Resources paired with their worths`() {
        val resources = (1..6).map { createTestResource("Food$it") }
        val matches = DependencyMatches(0)
        resources.forEachIndexed { i, resource -> matches.add(resource, i + 1) }

        matches.removeAll(listOf(resources[1], resources[3], createTestResource("Grass")))

        assertEquals(4, matches.size)
        val kept = matches.toList()
        assertEquals(listOf(1, 3, 5, 6), kept.map { it.second })
        assertTrue(kept.all { (resource, worth) -> resource === resources[worth - 1] })
    }

    @Test
    fun `removeAll without matching Resources keeps everything`() {
        val resources = (1..3).map { createTestResource("Food$it") }
        val matches = DependencyMatches(0)
        resources.forEachIndexed { i, resource -> matches.add(resource, i + 1) }

        matches.removeAll(listOf(createTestResource("Food1")))

        assertEquals(resources.zip(1..3).map { it.first to it.second }, matches.toList())
    }
}
