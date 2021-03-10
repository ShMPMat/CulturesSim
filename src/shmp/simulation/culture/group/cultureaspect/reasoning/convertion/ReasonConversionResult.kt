package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.Reasoning


data class ReasonConversionResult(val reasonings: MutableList<Reasoning>, val concepts: MutableList<ReasonConcept>) {
    constructor(reasonings: List<Reasoning>) : this(reasonings.toMutableList(), mutableListOf())
    constructor(reasoning: Reasoning) : this(mutableListOf(reasoning), mutableListOf())
    constructor(reasoning: Reasoning, concept: ReasonConcept) : this(mutableListOf(reasoning), mutableListOf(concept))

    fun isEmpty() = reasonings.isEmpty() && concepts.isEmpty()
    fun isNotEmpty() = !isEmpty()

    operator fun plusAssign(other: ReasonConversionResult) {
        reasonings.addAll(other.reasonings)
        concepts.addAll(other.concepts)
    }
}

fun Reasoning?.toConversionResult() = this
        ?.let { ReasonConversionResult(this) }
        ?: emptyReasonConversionResult()

fun emptyReasonConversionResult() = ReasonConversionResult(mutableListOf(), mutableListOf())
