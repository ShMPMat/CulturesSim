package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.random.randomElement
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.generator.culture.worldview.Meme
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
    return BaseReasoning(
            Meme("${concept.meme} is ${conceptToAdjectiveString(adjectiveConcept)}"),
            listOf(concept.meme, adjectiveConcept.meme),
            listOf(concept.toConclusion(conceptChange), adjectiveConcept.toConclusion(adjectiveConceptChange))
    )
}

fun makeAdjectiveReasoning(
        concept: ReasonConcept,
        adjectiveConcepts: List<ReasonConcept>,
        isPositive: Boolean,
        random: Random
) =
        randomElement(adjectiveConcepts.map { makeAdjectiveReasoning(concept, it, isPositive, random) }, random)

fun conceptToAdjectiveString(concept: ReasonConcept) = when (concept) {
    Importance -> "important"
    Unimportance -> "unimportant"
    Uniqueness -> "unique"
    Commonness -> "common"
    Simplicity -> "simple"
    Complexity -> "complex"
    Simpleness -> "easy"
    Hardness -> "hard"

    else -> throw ReasoningError("No adjective for $concept")
}
