package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.Reasoning

data class ReasonConversionResult(val reasonings: MutableList<Reasoning>, val concepts: MutableList<ReasonConcept>) {
    fun isEmpty() = reasonings.isEmpty() && concepts.isEmpty()
    fun isNotEmpty() = !isEmpty()

    operator fun plusAssign(other: ReasonConversionResult) {
        reasonings.addAll(other.reasonings)
        concepts.addAll(other.concepts)
    }
}

fun singletonReasonAdditionResult(reasoning: Reasoning, concept: ReasonConcept) =
        ReasonConversionResult(mutableListOf(reasoning), mutableListOf(concept))

fun emptyReasonAdditionResult() = ReasonConversionResult(mutableListOf(), mutableListOf())