package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.random.randomElement
import shmp.random.randomElementOrNull
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.DeterminedConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept
import shmp.simulation.culture.thinking.meaning.Meme
import kotlin.random.Random


fun generateBaseReasoning(reasonerMemes: List<Meme>): Reasoning {
    val random = RandomSingleton.random

    val positiveConcepts = listOf(Importance, Uniqueness, Commonness)
    val negativeConcepts = listOf(Unimportance)

    val reasonerPredicates = reasonerMemes.randomElementOrNull()?.let {
        listOf<Reasoning>(
                makeAdjectiveReasoning(ObjectConcept.ArbitraryObjectConcept(it), positiveConcepts, true, random),
                makeAdjectiveReasoning(ObjectConcept.ArbitraryObjectConcept(it), negativeConcepts, false, random)
        )
    } ?: listOf()

    return randomElement(
            listOf(
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
            ) + reasonerPredicates,
            random
    )
}


fun generateNewReasonings(field: ReasonField, complex: ReasonComplex): List<Reasoning> {
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
