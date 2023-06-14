package io.tashtabash.generator.culture.worldview.reasoning.concept

import io.tashtabash.generator.culture.worldview.reasoning.ReasoningError


fun conceptToAdjectiveString(concept: ReasonConcept) = when (concept) {
    IdeationalConcept.Importance -> "important"
    IdeationalConcept.Unimportance -> "unimportant"
    IdeationalConcept.Uniqueness -> "unique"
    IdeationalConcept.Commonness -> "common"
    IdeationalConcept.Simplicity -> "simple"
    IdeationalConcept.Complexity -> "complex"
    IdeationalConcept.Simpleness -> "easy"
    IdeationalConcept.Hardness -> "hard"
    IdeationalConcept.Mortality -> "mortal"
    IdeationalConcept.Immortality -> "immortal"

    else -> throw ReasoningError("No adjective for $concept")
}
