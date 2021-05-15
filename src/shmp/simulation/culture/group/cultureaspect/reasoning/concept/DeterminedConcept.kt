package shmp.simulation.culture.group.cultureaspect.reasoning.concept

import shmp.simulation.culture.thinking.meaning.Meme


class DeterminedConcept(val objectConcept: ObjectConcept, val ideationalConcept: IdeationalConcept) : ReasonConcept {
    override val meme = Meme("$objectConcept\'s $ideationalConcept")
    override val oppositeConcepts = objectConcept.oppositeConcepts + ideationalConcept.oppositeConcepts
    override val correspondingConcepts = objectConcept.correspondingConcepts + ideationalConcept.correspondingConcepts
}
