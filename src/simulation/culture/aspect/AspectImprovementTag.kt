package simulation.culture.aspect

import simulation.culture.aspect.labeler.AspectLabeler
import simulation.space.resource.tag.ResourceTag

class AspectImprovementTag(
        val labeler: AspectLabeler,
        val improvement: Double
) : ResourceTag("$labeler - $improvement")