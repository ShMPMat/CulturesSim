package simulation.culture.aspect

import simulation.culture.aspect.labeler.makeAspectLabeler
import simulation.space.resource.Genome
import simulation.space.resource.Resource
import simulation.space.resource.instantiation.DefaultTagParser
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.ResourceLabeler

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

private fun List<ResourceTag>.getAspectImprovement(aspect: Aspect) = this
        .filterIsInstance<AspectImprovementTag>()
        .filter { it.labeler.isSuitable(aspect) }
        .map { it.improvement }
        .foldRight(0.0, Double::plus)

class AspectImprovementLabeler(val aspect: Aspect): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.tags.getAspectImprovement(aspect) > 0

    override fun toString() = "Resource improves Aspect ${aspect.name}"
}
