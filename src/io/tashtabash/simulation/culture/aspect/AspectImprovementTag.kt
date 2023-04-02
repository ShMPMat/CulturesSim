package io.tashtabash.simulation.culture.aspect

import io.tashtabash.simulation.culture.aspect.labeler.AspectLabeler
import io.tashtabash.simulation.space.resource.tag.ResourceTag


class AspectImprovementTag(
        val labeler: AspectLabeler,
        val improvement: Double
) : ResourceTag("$labeler - $improvement") {
    override fun copy(level: Double) = AspectImprovementTag(labeler, level)
}
