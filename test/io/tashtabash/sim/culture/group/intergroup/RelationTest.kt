package io.tashtabash.sim.culture.group.intergroup

import io.mockk.mockk
import io.tashtabash.sim.culture.group.centers.Group
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test


class RelationTest {
    @Test
    fun `value can't go larger than 1`() {
        val relation = Relation(mockk<Group>(), mockk<Group>())
        relation.positive += 100_000_000

        assertTrue(relation.positive == 1.0)
    }

    @Test
    fun `value can't go lower than -1`() {
        val relation = Relation(mockk<Group>(), mockk<Group>())
        relation.positive -= 100_000_000

        assertTrue(relation.positive == -1.0)
    }
}
