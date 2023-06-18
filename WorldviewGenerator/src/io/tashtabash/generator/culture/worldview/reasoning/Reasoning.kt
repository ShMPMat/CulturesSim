package io.tashtabash.generator.culture.worldview.reasoning

import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.utils.SoftValue


interface Reasoning {
    val meme: Meme
    val additionalMemes: List<Meme>

    val conclusions: List<ReasonConclusion>

    fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): Reasoning?
}

abstract class AbstractReasoning : Reasoning {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractReasoning) return false

        if (meme != other.meme) return false

        return true
    }

    override fun hashCode() = meme.hashCode()

    override fun toString() = meme.toString()
}

data class ReasonConclusion(val concept: ReasonConcept, val value: SoftValue)

fun ReasonConcept.toConclusion(value: Double) = ReasonConclusion(this, SoftValue(value))
fun ReasonConcept.toConclusion(value: SoftValue) = toConclusion(value.actualValue)
