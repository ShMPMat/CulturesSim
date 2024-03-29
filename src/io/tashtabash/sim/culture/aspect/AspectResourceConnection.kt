package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.culture.aspect.labeler.makeAspectLabeler
import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.instantiation.tag.DefaultTagParser
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler


class AspectResourceTagParser(allowedTags: Collection<ResourceTag>) : DefaultTagParser(allowedTags) {
    override fun parse(key: Char, tag: String) = super.parse(key, tag) ?: when (key) {
        '&' -> {
            val (aspectLabeler, level) = tag.split("-".toRegex()).toTypedArray()

            wrapInTemplate(
                    AspectImprovementTag(makeAspectLabeler(aspectLabeler.split(";")), 1.0),
                    level
            )
        }
        else -> null
    }
}

fun Resource.getAspectImprovement(aspect: Aspect) = amount.toDouble() * tags.getAspectImprovement(aspect)

private fun Set<ResourceTag>.getAspectImprovement(aspect: Aspect) = filterIsInstance<AspectImprovementTag>()
        .filter { it.labeler.isSuitable(aspect) }
        .sumOf { it.improvement }

class AspectImprovementLabeler(val aspect: Aspect) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.tags.getAspectImprovement(aspect) > 0

    override fun toString() = "Resource improves Aspect ${aspect.name}"
}
