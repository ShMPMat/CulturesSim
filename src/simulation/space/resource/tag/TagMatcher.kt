package simulation.space.resource.tag

import simulation.space.resource.tag.labeler.ResourceTagLabeler

data class TagMatcher(val tag: ResourceTag, val labeler: ResourceTagLabeler)