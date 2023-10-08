package io.tashtabash.sim.space.resource.tag

import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.sim.space.resource.tag.leveler.ResourceLeveler


data class TagMatcher(
    val tag: ResourceTag,
    val labeler: ResourceLabeler,
    val Leveler: ResourceLeveler,
    val wipeOnMismatch: Boolean
)
