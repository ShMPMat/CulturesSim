package shmp.simulation.culture.group.cultureaspect.concept

import shmp.simulation.culture.thinking.meaning.Meme


interface ReasonConcept {
    val meme: Meme

    val oppositeConcepts: List<ReasonConcept>
    val correspondingConcepts: List<ReasonConcept>
}

abstract class AbstractReasonConcept : ReasonConcept {
    override fun toString() = meme.toString()
}
