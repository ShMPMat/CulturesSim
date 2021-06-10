package shmp.simulation.space.resource.tag

import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import shmp.simulation.space.resource.tag.leveler.ResourceLeveler


data class TagMatcher(val tag: ResourceTag, val labeler: ResourceLabeler, val Leveler: ResourceLeveler)
