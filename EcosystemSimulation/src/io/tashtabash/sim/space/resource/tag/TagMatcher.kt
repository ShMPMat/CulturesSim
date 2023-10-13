package io.tashtabash.sim.space.resource.tag

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.sim.space.resource.tag.leveler.ResourceLeveler
import kotlin.math.max


data class TagMatcher(
    val tag: ResourceTag,
    val labeler: ResourceLabeler,
    val leveler: ResourceLeveler,
    val wipeOnMismatch: Boolean
) {
    fun updateGenome(genome: Genome, tagsMap: MutableMap<ResourceTag, ResourceTag>) {
        val level = leveler.getLevel(genome)
        if (labeler.isSuitable(genome))
            tagsMap[tag] = tag.copy(level = max(level, tagsMap[tag]?.level ?: 0.0))
        else if (wipeOnMismatch)
            tagsMap.remove(tag)
    }
}
