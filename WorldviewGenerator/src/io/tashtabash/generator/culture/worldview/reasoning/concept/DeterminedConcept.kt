package io.tashtabash.generator.culture.worldview.reasoning.concept

import io.tashtabash.generator.culture.worldview.Meme


class DeterminedConcept(val objectConcept: ObjectConcept, val ideationalConcept: IdeationalConcept) : ReasonConcept {
    override val meme = Meme("$objectConcept\'s $ideationalConcept")
    override val oppositeConcepts = objectConcept.oppositeConcepts + ideationalConcept.oppositeConcepts
    override val correspondingConcepts = objectConcept.correspondingConcepts + ideationalConcept.correspondingConcepts

    override fun copy() = DeterminedConcept(objectConcept, ideationalConcept)

    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): ReasonConcept {
        val newConcept = DeterminedConcept(
                substitutions[objectConcept]
                        ?.takeIf { it is ObjectConcept }
                        as ObjectConcept?
                        ?: objectConcept,
                substitutions[ideationalConcept]
                        ?.takeIf { it is IdeationalConcept }
                        as IdeationalConcept?
                        ?: ideationalConcept
        )

        return substitutions[newConcept] ?: newConcept
    }
}
