package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.instantiation.bracketSensitiveSplit
import shmp.simulation.space.resource.tag.ResourceTag


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

    return tags.map { tag -> getLeveler(tag.take(2), tag.drop(2)) }
}

private fun getLeveler(key: String, value: String): ResourceLeveler = when (key) {
    "i|" -> ConstLeveler(value.toDouble())
    "t|" -> TagLeveler(ResourceTag(value))
    "m(" -> MulLeveler(makeLevelerList(value.dropLast(1)))
    else -> throw RuntimeException("Wrong tag for a leveler - $key")
}
