package simulation.space.resource.tag.labeler

import simulation.space.resource.tag.*

fun makeLabeler(tags: Collection<String>): ResourceTagLabeler {
    val labelers = ArrayList<ResourceTagLabeler>()
    for (tag in tags) {
        labelers.add(getLabel(tag.take(2), tag.drop(2)))
    }
    return ConcatLabeler(labelers)
}

private fun getLabel(key: String, value: String): ResourceTagLabeler = when (key) {
    "t:" -> TagLabeler(ResourceTag(value))
    "<=" -> SmallerSizeLabeler(value.toDouble())
    ">=" -> BiggerSizeLabeler(value.toDouble())
    "<D" -> SmallerDensityLabeler(value.toDouble())
    "mv" -> IsMovableLabeler()
    "rs" -> IsResistingLabeler()
    "!!" -> NegateLabeler(getLabel(value.take(2), value.drop(2)))
    else -> throw RuntimeException("Wrong tag for a labeler")
}