package simulation.culture.group.centers

import java.util.*
import kotlin.math.pow


class TraitCenter private constructor(map: EnumMap<Trait, TraitValue>) {
    private val traitMap = EnumMap<Trait, TraitValue>(Trait::class.java)

    init {
        map.entries.forEach { (k, v) ->
            traitMap[k] = TraitValue(v.value)
        }
    }

    constructor() : this(EnumMap(Trait::class.java)) {
        Trait.values().forEach {
            traitMap[it] = TraitValue()
        }
    }

    fun value(trait: Trait): Double = traitMap.getValue(trait).value

    fun copy() = TraitCenter(traitMap)
}


enum class Trait {
    Peace,
    Expansion,
    Consolidation
}

class TraitValue(value: Double = 0.0) {
    var value = value
    set(value) {
        field = value
        if (value > 1.0) field = 1.0
        if (value < -1.0) field = -1.0
    }
}


interface TraitExtractor {
    fun extract(center: TraitCenter): Double
}

class GetTrait(private val trait: Trait): TraitExtractor {
    override fun extract(center: TraitCenter) = center.value(trait)
}

class WrapperExtractor(val action: (TraitCenter) -> Double): TraitExtractor {
    override fun extract(center: TraitCenter) = action(center)
}

operator fun TraitExtractor.times(other: TraitExtractor) = WrapperExtractor {center ->
    extract(center) * other.extract(center)
}

fun TraitExtractor.pow(t: Double) = WrapperExtractor { center -> extract(center).pow(t) }
