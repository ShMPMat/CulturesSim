package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.Reasoning

data class ReasonAdditionResult(val reasonings: MutableList<Reasoning>, val concepts: MutableList<ReasonConcept>) {
    fun isEmpty() = reasonings.isEmpty() && concepts.isEmpty()
    fun isNotEmpty() = !isEmpty()

    operator fun plusAssign(other: ReasonAdditionResult) {
        reasonings.addAll(other.reasonings)
        concepts.addAll(other.concepts)
    }
}

fun singletonReasonAdditionResult(reasoning: Reasoning, concept: ReasonConcept) =
        ReasonAdditionResult(mutableListOf(reasoning), mutableListOf(concept))

fun emptyReasonAdditionResult() = ReasonAdditionResult(mutableListOf(), mutableListOf())