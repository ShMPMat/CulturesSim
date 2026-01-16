package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.resource.tag.ResourceTag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class GenomeTest {
    @Test
    fun `baseName correctly appends the legacy postfix`() {
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
