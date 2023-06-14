package io.tashtabash.generator.culture.worldview.reasoning

import io.tashtabash.random.randomElement
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import kotlin.random.Random


fun makeAdjectiveReasoning(
        concept: ReasonConcept,
        adjectiveConcept: ReasonConcept,
        isPositive: Boolean,
        random: Random
): BaseReasoning {
    val conceptChange = if (isPositive)
        random.nextDouble(0.0, 1.0)
    else
        random.nextDouble(-1.0, 0.0)
    val adjectiveConceptChange = random.nextDouble(0.0, 0.1)

    return QualityReasoning(concept, adjectiveConcept, conceptChange, adjectiveConceptChange)
}

fun makeAdjectiveReasoning(
        concept: ReasonConcept,
        adjectiveConcepts: List<ReasonConcept>,
        isPositive: Boolean,
        random: Random
) = randomElement(adjectiveConcepts.map { makeAdjectiveReasoning(concept, it, isPositive, random) }, random)
