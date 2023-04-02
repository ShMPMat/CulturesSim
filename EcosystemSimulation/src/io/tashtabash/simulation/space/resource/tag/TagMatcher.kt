package io.tashtabash.simulation.space.resource.tag

import io.tashtabash.simulation.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.simulation.space.resource.tag.leveler.ResourceLeveler


data class TagMatcher(val tag: ResourceTag, val labeler: ResourceLabeler, val Leveler: ResourceLeveler)
