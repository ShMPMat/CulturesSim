package simulation.culture.group.process.action

import simulation.culture.group.centers.Trait
import simulation.culture.group.centers.TraitCenter
import kotlin.math.pow


interface TraitExtractor {
    fun extract(center: TraitCenter): Double
}


private class GetTrait(private val trait: Trait) : TraitExtractor {
    override fun extract(center: TraitCenter) = center.value(trait)

    override fun toString() = "trait $trait"
}

fun Trait.get(): TraitExtractor = GetTrait(this)


private class WrapperExtractor(private val description: String, val action: (TraitCenter) -> Double) : TraitExtractor {
    override fun extract(center: TraitCenter) = action(center)

    override fun toString() = description
}

operator fun TraitExtractor.times(other: TraitExtractor): TraitExtractor =
        WrapperExtractor("($this) times ($other)") { center ->
            extract(center) * other.extract(center)
        }

fun TraitExtractor.pow(t: Double): TraitExtractor = WrapperExtractor("$this to the power of $t") {
    center -> extract(center).pow(t)
}
