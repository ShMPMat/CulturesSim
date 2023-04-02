package io.tashtabash.simulation.culture.group.process

import io.tashtabash.simulation.culture.group.centers.Trait
import io.tashtabash.simulation.culture.group.centers.TraitCenter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


interface TraitExtractor {
    fun extract(center: TraitCenter): Double
}

private class ConstExtractor(val t: Double): TraitExtractor {
    override fun extract(center: TraitCenter) = t
}

fun Double.toExtractor(): TraitExtractor = ConstExtractor(this)


private class WrapperExtractor(private val description: String, val action: (TraitCenter) -> Double) : TraitExtractor {
    override fun extract(center: TraitCenter) = action(center)

    override fun toString() = description
}

operator fun TraitExtractor.times(other: TraitExtractor): TraitExtractor =
        WrapperExtractor("($this) times ($other)") { center ->
            extract(center) * other.extract(center)
        }

operator fun TraitExtractor.times(t: Double): TraitExtractor = WrapperExtractor("$this * $t") { center ->
    extract(center) * t
}

operator fun TraitExtractor.times(n: Int): TraitExtractor = WrapperExtractor("$this * $n") { center ->
    extract(center) * n
}

operator fun TraitExtractor.div(t: Double): TraitExtractor = WrapperExtractor("$this * $t") { center ->
    extract(center) / t
}

operator fun TraitExtractor.div(n: Int): TraitExtractor = WrapperExtractor("$this * $n") { center ->
    extract(center) / n
}

fun TraitExtractor.pow(t: Double): TraitExtractor = WrapperExtractor("$this to the power of $t") { center ->
    extract(center).pow(t)
}
fun TraitExtractor.pow(n: Int) = this.pow(n.toDouble())

fun TraitExtractor.reverse(): TraitExtractor = WrapperExtractor("reverse of ($this)") { center ->
    max(1 - extract(center), 0.0)
}

fun max(first: TraitExtractor, second: TraitExtractor): TraitExtractor =
        WrapperExtractor("max of $first and $second") { center ->
            max(first.extract(center), second.extract(center))
        }


private class GetTrait(private val trait: Trait) : TraitExtractor {
    override fun extract(center: TraitCenter) = center.normalValue(trait)

    override fun toString() = "trait $trait"
}

fun Trait.get(): TraitExtractor = GetTrait(this)

fun Trait.getPositive(): TraitExtractor = WrapperExtractor("positive of $this") { center ->
    max(center.processedValue(this), 0.0)
}

fun Trait.getNegative(): TraitExtractor = WrapperExtractor("negative of $this") { center ->
    -min(center.processedValue(this), 0.0)
}


object PassingExtractor : TraitExtractor {
    override fun extract(center: TraitCenter) = 1.0
}
