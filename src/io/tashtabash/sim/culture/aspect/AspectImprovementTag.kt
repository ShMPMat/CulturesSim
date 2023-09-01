package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.culture.aspect.labeler.AspectLabeler
import io.tashtabash.sim.space.resource.tag.ResourceTag


class AspectImprovementTag(
        val labeler: AspectLabeler,
        val improvement: Double
) : ResourceTag("$labeler - $improvement") {
    override fun copy(level: Double) = AspectImprovementTag(labeler, level)
}
