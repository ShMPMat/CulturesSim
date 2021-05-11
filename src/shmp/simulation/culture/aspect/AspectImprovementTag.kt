package shmp.simulation.culture.aspect

import shmp.simulation.culture.aspect.labeler.AspectLabeler
import shmp.simulation.space.resource.tag.ResourceTag


class AspectImprovementTag(
        val labeler: AspectLabeler,
        val improvement: Double
) : ResourceTag("$labeler - $improvement")
