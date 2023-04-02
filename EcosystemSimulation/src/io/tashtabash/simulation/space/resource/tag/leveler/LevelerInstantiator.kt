package io.tashtabash.simulation.space.resource.tag.leveler

import io.tashtabash.simulation.space.resource.instantiation.bracketSensitiveSplit
import io.tashtabash.simulation.space.resource.tag.ResourceTag


fun makeResourceLeveler(tagString: String): ResourceLeveler {
    val levelers = makeLevelerList(tagString)

    return when {
        levelers.isEmpty() -> ConstLeveler(1.0)
        levelers.size == 1 -> levelers[0]
        else -> SumLeveler(levelers)
    }
}

private fun makeLevelerList(tagString: String): List<ResourceLeveler> {
    val tags = tagString.bracketSensitiveSplit(',')

    return tags.map { getLeveler(it) }
}

private fun getLeveler(tag: String): ResourceLeveler {
    tag.toDoubleOrNull()?.let {
        return ConstLeveler(it)
    }

    val key = tag.take(2)
    val value = tag.drop(2)

    return when (key) {
        "i|" -> ConstLeveler(value.toDouble())
        "t|" -> TagLeveler(ResourceTag(value))
        "m(" -> MulLeveler(makeLevelerList(value.dropLast(1)))
        else -> if (tag.matches(Regex("[a-zA-Z]+")))
            TagLeveler(ResourceTag(tag))
        else throw RuntimeException("Wrong tag for a leveler - $key")
    }
}
