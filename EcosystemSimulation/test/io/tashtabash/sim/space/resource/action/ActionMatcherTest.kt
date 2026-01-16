package io.tashtabash.sim.space.resource.action

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.ResourceCore
import io.tashtabash.sim.space.resource.createTestGenome
import io.tashtabash.sim.space.resource.tag.labeler.BaseNameLabeler
import io.tashtabash.sim.space.resource.tag.labeler.PassingLabeler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class ActionMatcherTest {
    private val actionName = "Grow"
    private val action = ResourceAction(actionName, listOf(), listOf())

    @Test
    fun `init throws an error if the results are empty`() {
        assertThrows<DataInitializationError> {
            ActionMatcher(PassingLabeler, listOf(), actionName)
        }
    }

    @Test
    fun `match returns false if resource already has the action`() {
        val matcher = ActionMatcher(PassingLabeler, listOf("Plant" to 1), actionName)
        val resource = Resource(
            ResourceCore(
                createTestGenome("Seed", actions = mapOf(action to mutableListOf()))
            )
        )

        assertFalse(matcher.match(resource))
    }

    @Test
    fun `match returns false if the resource name is in the results list`() {
        val matcher = ActionMatcher(PassingLabeler, listOf("Plant" to 1), actionName)
        val resource = Resource(ResourceCore(createTestGenome()))

        assertFalse(matcher.match(resource))
    }

    @Test
    fun `match returns false if the labeler doesn't match`() {
        val matcher = ActionMatcher(BaseNameLabeler("NotSeed"), listOf("Plant" to 1), actionName)
        val resource = Resource(ResourceCore(createTestGenome("Seed")))

        assertFalse(matcher.match(resource))
    }

    @Test
    fun `match returns true if the labeler matches`() {
        val matcher = ActionMatcher(BaseNameLabeler("Seed"), listOf("Plant" to 1), actionName)
        val resource = Resource(ResourceCore(createTestGenome("Seed")))

        assertTrue(matcher.match(resource))
    }
}
