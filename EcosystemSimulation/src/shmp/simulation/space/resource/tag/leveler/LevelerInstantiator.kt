package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.instantiation.bracketSensitiveSplit
import shmp.simulation.space.resource.tag.ResourceTag


fun makeResourceLeveler(tagString: String): ResourceLeveler {
    val tags = tagString.bracketSensitiveSplit(',')
    val levelers = ArrayList<ResourceLeveler>()
    for (tag in tags)
        levelers.add(getLeveler(tag.take(2), tag.drop(2)))

    return when {
        levelers.isEmpty() -> ConstLeveler(1)
        levelers.size == 1 -> levelers[0]
        else -> SumLeveler(levelers)
    }
}

private fun getLeveler(key: String, value: String): ResourceLeveler = when (key) {
    "i|" -> ConstLeveler(value.toInt())
    "t|" -> TagLeveler(ResourceTag(value))
    else -> throw RuntimeException("Wrong tag for a leveler - $key")
}
