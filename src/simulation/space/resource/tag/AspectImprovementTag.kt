package simulation.space.resource.tag

import simulation.culture.aspect.labeler.AspectLabeler

class AspectImprovementTag(
        val labeler: AspectLabeler,
        val improvement: Double
) : ResourceTag("$labeler - $improvement")