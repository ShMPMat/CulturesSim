package shmp.generator.culture.worldview.reasoning

import shmp.random.randomElement
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElementOrNull
import shmp.generator.culture.worldview.reasoning.concept.DeterminedConcept
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import shmp.generator.culture.worldview.reasoning.concept.ObjectConcept
import shmp.generator.culture.worldview.Meme
import shmp.random.singleton.randomElement
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
