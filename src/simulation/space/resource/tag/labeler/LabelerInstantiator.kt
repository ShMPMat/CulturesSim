package simulation.space.resource.tag.labeler

import simulation.space.resource.ResourceType
import simulation.space.resource.instantiation.bracketSensitiveSplit
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.material.Material

fun makeResourceLabeler(tagString: String): ResourceLabeler {
    val tags = tagString.bracketSensitiveSplit(',')
    val labelers = ArrayList<ResourceLabeler>()
    for (tag in tags)
        labelers.add(getLabel(tag.take(2), tag.drop(2)))
    return ConcatLabeler(labelers)
}

private fun getLabel(key: String, value: String): ResourceLabeler = when (key) {
    "t:" -> TagLabeler(ResourceTag(value))
    "T:" -> TypeLabeler(ResourceType.valueOf(value))
    "m:" -> PrimaryMaterialLabeler(Material(value, 0.0, listOf()))
    "r:" -> SimpleNameLabeler(value)
    "<=" -> SmallerSizeLabeler(value.toDouble())
    ">=" -> BiggerSizeLabeler(value.toDouble())
    "<D" -> SmallerDensityLabeler(value.toDouble())
    "mv" -> IsMovableLabeler()
    "rs" -> IsResistingLabeler()
    "!!" -> NegateLabeler(getLabel(value.take(2), value.drop(2)))
    "P(" -> AnyPartLabeler(getLabel(value.take(2), value.drop(2).dropLast(1)))
    "A(" -> AnyPartOrSelfLabeler(getLabel(value.take(2), value.drop(2).dropLast(1)))
    else -> throw RuntimeException("Wrong tag for a labeler")
}
