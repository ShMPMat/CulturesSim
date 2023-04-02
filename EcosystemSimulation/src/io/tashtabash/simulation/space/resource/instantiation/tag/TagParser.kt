package io.tashtabash.simulation.space.resource.instantiation.tag

import io.tashtabash.simulation.DataInitializationError
import io.tashtabash.simulation.space.resource.tag.ResourceTag
import io.tashtabash.simulation.space.resource.tag.leveler.makeResourceLeveler


interface TagParser {
    fun parse(key: Char, tag: String): TagTemplate?
}

open class DefaultTagParser(private val allowedTags: Collection<ResourceTag>) : TagParser {
    override fun parse(key: Char, tag: String) = when (key) {
        '$' -> {
            val (name, level) =
                    if (tag.contains(':'))
                        tag.split(":".toRegex()).toList()
                    else listOf(tag, "1")
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
