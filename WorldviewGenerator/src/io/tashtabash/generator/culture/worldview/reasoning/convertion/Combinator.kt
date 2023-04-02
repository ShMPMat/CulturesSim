package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept


data class Combinator(
        val startConcepts: Set<ReasonConcept>,
        val result: ReasonConcept,
        override val probability: Double
): SampleSpaceObject

data class SemiCombinator(val startConcepts: Set<ReasonConcept>, val result: ReasonConcept)


infix fun ReasonConcept.leadsTo(result: ReasonConcept) =
        SemiCombinator(setOf(this), result)
infix fun Set<ReasonConcept>.leadsTo(result: ReasonConcept) =
        SemiCombinator(this, result)
infix fun Pair<ReasonConcept, ReasonConcept>.leadsTo(result: ReasonConcept) =
        SemiCombinator(setOf(first, second), result)

infix fun SemiCombinator.chance(probability: Double) = Combinator(startConcepts, result, probability)
