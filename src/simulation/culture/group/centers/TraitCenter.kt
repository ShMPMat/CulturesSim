package simulation.culture.group.centers

import java.util.*


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

    fun normalValue(trait: Trait) = (value(trait) + 1) / 2

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
