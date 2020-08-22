package simulation.culture.group.centers

import java.util.*
import kotlin.math.abs


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

    fun changeOn(traitChange: TraitChange) {
        val (trait, delta) = traitChange
        val ratio = 1 - abs(traitMap.getValue(trait).value)

        traitMap.getValue(trait).value += delta * ratio
    }

    fun changeOnAll(traitChanges: List<TraitChange>) = traitChanges.forEach { changeOn(it) }

    fun copy() = TraitCenter(traitMap)

    override fun toString() = traitMap.entries.joinToString { (t, v) -> "$t = ${v.value}" }
}


enum class Trait {
    Peace,
    Expansion,
    Consolidation,
    Creation
}

class TraitValue(value: Double = 0.0) {
    var value = value
        set(value) {
            field = value
            if (value > 1.0) field = 1.0
            if (value < -1.0) field = -1.0
        }
}


data class TraitChange(val trait: Trait, val delta: Double) {
    operator fun times(t: Double) = TraitChange(trait, delta * t)
}

fun makePositiveChange(trait: Trait) = TraitChange(trait, 0.01)
fun makeNegativeChange(trait: Trait) = TraitChange(trait, -0.01)
