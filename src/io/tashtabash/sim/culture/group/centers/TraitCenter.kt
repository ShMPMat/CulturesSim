package io.tashtabash.sim.culture.group.centers

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.utils.SoftValue
import java.util.*


class TraitCenter private constructor(map: EnumMap<Trait, TraitValue>) {
    private val traitMap = EnumMap<Trait, TraitValue>(Trait::class.java)

    init {
        map.entries.forEach { (k, v) ->
            traitMap[k] = TraitValue(v.value)
        }
    }

    constructor() : this(EnumMap(Trait::class.java)) {
        for (trait in Trait.values()) {
            val traitSeed = RandomSingleton.random.nextDouble(-1.0, 1.0)
            traitMap[trait] = TraitValue(traitSeed * session.defaultGroupTraitSpread)
        }
    }

    fun value(trait: Trait): TraitValue = traitMap.getValue(trait)

    fun processedValue(trait: Trait): Double = value(trait).value

    fun normalValue(trait: Trait) = value(trait).norm

    internal fun changeOn(traitChange: TraitChange) {
        val (trait, delta) = traitChange

        traitMap[trait] = traitMap.getValue(trait) + TraitValue(delta)
    }

    internal fun changeOnAll(traitChanges: List<TraitChange>) = traitChanges.forEach { changeOn(it) }

    fun copy() = TraitCenter(traitMap)

    override fun toString() = traitMap.entries.joinToString { (t, v) -> "$t = ${v.value}" }
}


enum class Trait(val positiveMeme: Meme, val negativeMeme: Meme) {
    Peace(Meme("Peace"), Meme("War")),
    Expansion(Meme("Expansion"), Meme("Content")),
    Consolidation(Meme("Consolidation"), Meme("Freedom")),
    Creation(Meme("Creation"), Meme("Destruction")),
    Discovery(Meme("Discovery"), Meme("Passiveness"))
}

typealias TraitValue = SoftValue

data class TraitChange(val trait: Trait, val delta: Double) {
    operator fun times(t: Double) = TraitChange(trait, delta * t)
    operator fun times(n: Int) = TraitChange(trait, delta * n)

    override fun toString() = "$trait on amount  %.3f".format(delta)
}

fun Trait.toPositiveChange() = TraitChange(this, 0.01)
fun Trait.toNegativeChange() = TraitChange(this, -0.01)
fun Trait.toChange(value: Double) = TraitChange(this, value)
fun Trait.toChange(value: SoftValue) = toChange(value.actualValue)
