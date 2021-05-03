package shmp.simulation.space.resource.tag

import shmp.simulation.space.resource.tag.labeler.ResourceLabeler


data class TagMatcher(val tag: ResourceTag, val labeler: ResourceLabeler)
