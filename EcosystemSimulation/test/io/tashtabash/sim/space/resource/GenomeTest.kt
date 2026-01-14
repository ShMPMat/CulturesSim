package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.tag.ResourceTag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class GenomeTest {
    private fun createTestGenome(
        name: String = "TestResource",
        legacy: String? = null,
        sizeRange: Pair<Double, Double> = 1.0 to 1.0,
        primaryMaterial: Material? = null,
        tags: Set<ResourceTag> = emptySet()
    ): Genome {
        return Genome(
            name = name,
            type = ResourceType.Plant,
            sizeRange = sizeRange,
            spreadProbability = 0.5,
            baseDesirability = 10,
            isMutable = false,
            isMovable = false,
            behaviour = Behaviour(0.0, 0.0, 0.0, 0.0, OverflowType.Cut),
            appearance = Appearance(null, null, null),
            hasLegacy = legacy != null,
            lifespan = 100.0,
            defaultAmount = 10,
            legacy = legacy,
            dependencies = emptyList(),
            tags = tags,
            primaryMaterial = primaryMaterial,
            secondaryMaterials = emptyList(),
            conversionCore = ConversionCore(mapOf())
        )
    }

    @Test
    fun `baseName correctly appends legacy postfix`() {
        val genomeWithLegacy = createTestGenome(name = "Iron", legacy = "Star")
        val genomeWithoutLegacy = createTestGenome(name = "Iron", legacy = null)

        assertEquals("Iron_of_Star", genomeWithLegacy.baseName)
        assertEquals("Iron", genomeWithoutLegacy.baseName)
    }

    @Test
    fun `copy creates a new instance with identical values but allows overrides`() {
        val original = createTestGenome(name = "Corral")
        val copied = original.copy(name = "Cool Corral")

        assertEquals("Corral", original.name)
        assertEquals("Cool Corral", copied.name)
        assertEquals(original.type, copied.type)
    }

    @Test
    fun `getTagLevel returns 0 for non-existent tags`() {
        val genome = createTestGenome()
        val missingTag = ResourceTag("taggy")

        assertEquals(0.0, genome.getTagLevel(missingTag))
    }
}