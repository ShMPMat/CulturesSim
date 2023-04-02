package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*


val combinators = mutableListOf(
        Rareness   to Good leadsTo Luck       chance 0.9,
        Rareness   to Bad  leadsTo Misfortune chance 0.9,
        Uniqueness to Good leadsTo Luck       chance 0.9,
        Uniqueness to Bad  leadsTo Misfortune chance 0.9,
)
