package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.reasoning.Reasoning


data class ReasonConversionResult(val reasonings: MutableList<Reasoning>, val concepts: MutableList<ReasonConcept>) {
    constructor(reasonings: List<Reasoning>) : this(reasonings.toMutableList(), mutableListOf())
    constructor(reasoning: Reasoning) : this(mutableListOf(reasoning), mutableListOf())
    constructor(reasoning: Reasoning, concept: ReasonConcept) : this(mutableListOf(reasoning), mutableListOf(concept))
    constructor(concept: ReasonConcept) : this(mutableListOf(), mutableListOf(concept))

    fun isEmpty() = reasonings.isEmpty() && concepts.isEmpty()
    fun isNotEmpty() = !isEmpty()

    operator fun plus(other: ReasonConversionResult) = ReasonConversionResult(
            (reasonings + other.reasonings).toMutableList(),
            (concepts + other.concepts).toMutableList()
    )
}

fun Reasoning?.toConversionResult() = this
        ?.let { ReasonConversionResult(this) }
        ?: emptyReasonConversionResult()

fun emptyReasonConversionResult() = ReasonConversionResult(mutableListOf(), mutableListOf())
