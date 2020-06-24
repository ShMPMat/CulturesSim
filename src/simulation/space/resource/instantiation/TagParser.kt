package simulation.space.resource.instantiation

import simulation.SimulationException
import simulation.space.resource.tag.ResourceTag

interface TagParser {
    fun parse(key: Char, tag: String): ResourceTag?
}

open class DefaultTagParser(private val allowedTags: Collection<ResourceTag>): TagParser {
    override fun parse(key: Char, tag: String) = when (key) {
        '$' -> {
            val elements = tag.split(":".toRegex()).toTypedArray()
            val resourceTag = ResourceTag(elements[0], elements[1].toInt())
            if (!allowedTags.contains(resourceTag)) throw SimulationException("Tag $resourceTag doesnt exist")
            resourceTag
        }
        else -> null
    }
}