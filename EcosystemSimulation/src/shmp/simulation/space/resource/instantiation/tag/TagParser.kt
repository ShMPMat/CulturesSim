package shmp.simulation.space.resource.instantiation.tag

import shmp.simulation.DataInitializationError
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.leveler.makeResourceLeveler


interface TagParser {
    fun parse(key: Char, tag: String): TagTemplate?
}

open class DefaultTagParser(private val allowedTags: Collection<ResourceTag>): TagParser {
    override fun parse(key: Char, tag: String) = when (key) {
        '$' -> {
            val (name, level) = tag.split(":".toRegex()).toTypedArray()
            val resourceTag = ResourceTag(name)
            if (!allowedTags.contains(resourceTag))
                throw DataInitializationError("Tag $resourceTag doesnt exist")

            wrapInTemplate(resourceTag, level)
        }
        else -> null
    }

    protected fun wrapInTemplate(resourceTag: ResourceTag, levelString: String) =
            TagTemplate(resourceTag, makeResourceLeveler(levelString))
}
