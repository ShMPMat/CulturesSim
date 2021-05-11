package shmp.simulation.culture.aspect

import shmp.simulation.culture.aspect.labeler.makeAspectLabeler
import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.instantiation.DefaultTagParser
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler


class AspectResourceTagParser(allowedTags: Collection<ResourceTag>) : DefaultTagParser(allowedTags) {
    override fun parse(key: Char, tag: String) = super.parse(key, tag) ?: when (key) {
        '&' -> {
            val elements = tag.split("-".toRegex()).toTypedArray()
            AspectImprovementTag(
                    makeAspectLabeler(elements[0].split(";")),
                    elements[1].toDouble()
            )
        }
        else -> null
    }
}

fun Resource.getAspectImprovement(aspect: Aspect) = amount.toDouble() * tags.getAspectImprovement(aspect)

private fun Set<ResourceTag>.getAspectImprovement(aspect: Aspect) = this
        .filterIsInstance<AspectImprovementTag>()
        .filter { it.labeler.isSuitable(aspect) }
        .map { it.improvement }
        .foldRight(0.0, Double::plus)

class AspectImprovementLabeler(val aspect: Aspect): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.tags.getAspectImprovement(aspect) > 0

    override fun toString() = "Resource improves Aspect ${aspect.name}"
}
