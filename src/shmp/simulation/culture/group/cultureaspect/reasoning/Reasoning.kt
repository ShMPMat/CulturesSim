package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.generator.culture.worldview.Meme
import shmp.utils.SoftValue


interface Reasoning {
    val meme: Meme
    val additionalMemes: List<Meme>

    val conclusions: List<ReasonConclusion>
}

abstract class AbstractReasoning : Reasoning {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractReasoning) return false

        if (meme != other.meme) return false

        return true
    }

    override fun hashCode(): Int {
        return meme.hashCode()
    }

    override fun toString() = meme.toString()
}

data class ReasonConclusion(val concept: ReasonConcept, val value: SoftValue)

fun ReasonConcept.toConclusion(value: Double) = ReasonConclusion(this, SoftValue(value))
fun ReasonConcept.toConclusion(value: SoftValue) = toConclusion(value.actualValue)
