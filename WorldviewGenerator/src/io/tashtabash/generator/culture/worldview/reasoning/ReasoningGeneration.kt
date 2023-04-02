package io.tashtabash.generator.culture.worldview.reasoning

import io.tashtabash.random.randomElement
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.generator.culture.worldview.reasoning.concept.DeterminedConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.random.singleton.randomElement
import kotlin.random.Random


internal fun generateBaseReasoning(reasonerMemes: List<Meme>): Reasoning {
    val random = RandomSingleton.random

    val positiveConcepts = listOf(Importance, Uniqueness, Commonness)
    val negativeConcepts = listOf(Unimportance)

    val reasonerPredicates = reasonerMemes.randomElementOrNull()?.let {
        listOf<Reasoning>(
                makeAdjectiveReasoning(ObjectConcept.ArbitraryObjectConcept(it), positiveConcepts, true, random),
                makeAdjectiveReasoning(ObjectConcept.ArbitraryObjectConcept(it), negativeConcepts, false, random)
        )
    } ?: listOf()

    val possibleReasonings = listOf(
            makeAdjectiveReasoning(
                    generateDeterminedConceptFromMemes(reasonerMemes, listOf(Life, Death), random),
                    positiveConcepts,
                    true,
                    random
            ),
            makeAdjectiveReasoning(
                    generateDeterminedConceptFromMemes(reasonerMemes, listOf(Life, Death), random),
                    negativeConcepts,
                    false,
                    random
            )
    ) + reasonerPredicates

    return possibleReasonings.randomElement()
}


internal fun generateNewReasonings(field: ReasonField, complex: ReasonComplex): List<Reasoning> {
    return listOf()
}


private fun generateDeterminedConceptFromMemes(
        determinerMemes: List<Meme>,
        ideationalConcepts: List<IdeationalConcept>,
        random: Random
) = randomElement(listOf<Meme?>(null) + determinerMemes, random)?.let {
    DeterminedConcept(
            ObjectConcept.ArbitraryObjectConcept(it),
            randomElement(ideationalConcepts, random)
    )
} ?: randomElement(ideationalConcepts, random)
