package shmp.generator.culture.worldview.reasoning.concept

import shmp.generator.culture.worldview.Meme


class DeterminedConcept(val objectConcept: ObjectConcept, val ideationalConcept: IdeationalConcept) : ReasonConcept {
    override val meme = Meme("$objectConcept\'s $ideationalConcept")
    override val oppositeConcepts = objectConcept.oppositeConcepts + ideationalConcept.oppositeConcepts
    override val correspondingConcepts = objectConcept.correspondingConcepts + ideationalConcept.correspondingConcepts
}
