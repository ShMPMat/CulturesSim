package io.tashtabash.sim.culture.group.intergroup

import io.mockk.mockk
import io.tashtabash.sim.culture.group.centers.Group
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test


class RelationTest {
    @Test
    fun `value can't go larger than 1`() {
        val relation = Relation(mockk<Group>(), mockk<Group>())
        relation.value += 100_000_000

        assertTrue(relation.value == 1.0)
        assertTrue(relation.normalized == 1.0)
    }

    @Test
    fun `value can't go lower than -1`() {
        val relation = Relation(mockk<Group>(), mockk<Group>())
        relation.value -= 100_000_000

        assertTrue(relation.value == -1.0)
        assertTrue(relation.normalized == 0.0)
    }
}
