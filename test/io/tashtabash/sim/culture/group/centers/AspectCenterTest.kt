package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectCore
import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.space.resource.action.ResourceAction
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test


class AspectCenterTest {
    private val aspect = Aspect(
        AspectCore(
            "sing",
            listOf(),
            listOf(),
            false,
            false,
            0.1,
            listOf(),
            listOf(),
            ResourceAction("sing", listOf(), listOf())
        ),
        AspectDependencies(mutableMapOf())
    )
//    private val mockWrapper = mockk<ConverseWrapper>(relaxed = true)

    @Test
    fun `remove returns false when aspect is not in pool`() {
        val aspectCenter = AspectCenter()

        assertFalse(aspectCenter.remove(aspect))
    }

    @Test
    fun `remove returns true and clears pools when aspect exists`() {
        val aspectCenter = AspectCenter(listOf(aspect))

        val result = aspectCenter.remove(aspect)

        assertTrue(result)
        assertFalse(aspectCenter.aspectPool.contains(aspect))
    }
}
