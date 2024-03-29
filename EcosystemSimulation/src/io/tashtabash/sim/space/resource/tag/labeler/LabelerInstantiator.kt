package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.ResourceType
import io.tashtabash.sim.space.resource.instantiation.bracketSensitiveSplit
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.tile.Tile


fun makeResourceLabeler(tagString: String): ResourceLabeler {
    val tags = tagString.bracketSensitiveSplit(',')
    val labelers = ArrayList<ResourceLabeler>()
    for (tag in tags)
        labelers.add(getLabel(tag.take(2), tag.drop(2)))

    return when {
        labelers.isEmpty() -> PassingLabeler
        labelers.size == 1 -> labelers[0]
        else -> ConcatLabeler(labelers)
    }
}

private fun getLabel(key: String, value: String): ResourceLabeler = when (key) {
    "t:" -> TagLabeler(ResourceTag(value))
    "T:" -> TypeLabeler(ResourceType.valueOf(value))
    "m:" -> PrimaryMaterialLabeler(Material(value, 0.0, listOf()))
    "r:" -> SimpleNameLabeler(value)
    "h:" -> HabitatLabeler(Tile.Type.valueOf(value))
    "<=" -> SmallerSizeLabeler(value.toDouble())
    ">=" -> BiggerSizeLabeler(value.toDouble())
    "<D" -> SmallerDensityLabeler(value.toDouble())
    "mv" -> IsMovableLabeler()
    "rs" -> IsResistingLabeler()
    "!!" -> NegateLabeler(getLabel(value.take(2), value.drop(2)))
    "P(" -> AnyPartLabeler(getLabel(value.take(2), value.drop(2).dropLast(1)))
    "A(" -> AnyPartOrSelfLabeler(getLabel(value.take(2), value.drop(2).dropLast(1)))
    "D(" -> {
        val labelers = value.dropLast(1).split("||")
                .map { getLabel(it.take(2), it.drop(2)) }

        when {
            labelers.isEmpty() -> PassingLabeler
            labelers.size == 1 -> labelers[0]
            else -> DisjointLabeler(labelers)
        }
    }
    else -> throw RuntimeException("Wrong tag for a labeler '$key'")
}
