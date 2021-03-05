package shmp.simulation.culture.group.centers

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.utils.SoftValue
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

    internal fun changeOn(traitChange: TraitChange) {
        val (trait, delta) = traitChange

        traitMap[trait] = traitMap.getValue(trait) + TraitValue(delta)
    }

    internal fun changeOnAll(traitChanges: List<TraitChange>) = traitChanges.forEach { changeOn(it) }

    fun copy() = TraitCenter(traitMap)

    override fun toString() = traitMap.entries.joinToString { (t, v) -> "$t = ${v.value}" }
}


enum class Trait(val positiveMeme: Meme, val negativeMeme: Meme) {
    Peace(MemeSubject("Peace"), MemeSubject("War")),
    Expansion(MemeSubject("Expansion"), MemeSubject("Content")),
    Consolidation(MemeSubject("Consolidation"), MemeSubject("Freedom")),
    Creation(MemeSubject("Creation"), MemeSubject("Destruction"))
}

typealias TraitValue = SoftValue

data class TraitChange(val trait: Trait, val delta: Double) {
    operator fun times(t: Double) = TraitChange(trait, delta * t)

    override fun toString() = "$trait on amount  %.3f".format(delta)
}

fun Trait.toPositiveChange() = TraitChange(this, 0.01)
fun Trait.toNegativeChange() = TraitChange(this, -0.01)
fun Trait.toChange(value: Double) = TraitChange(this, value)
fun Trait.toChange(value: SoftValue) = toChange(value.actualValue)