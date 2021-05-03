package shmp.simulation.space.resource.instantiation

import shmp.simulation.DataInitializationError
import shmp.simulation.space.resource.tag.ResourceTag


interface TagParser {
    fun parse(key: Char, tag: String): ResourceTag?
}

open class DefaultTagParser(private val allowedTags: Collection<ResourceTag>): TagParser {
    override fun parse(key: Char, tag: String) = when (key) {
        '$' -> {
            val (name, level) = tag.split(":".toRegex()).toTypedArray()
            val resourceTag = ResourceTag(name, level.toInt())
            if (!allowedTags.contains(resourceTag))
                throw DataInitializationError("Tag $resourceTag doesnt exist")
            resourceTag
        }
        else -> null
    }
}
