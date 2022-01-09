package shmp.generator.culture.worldview.reasoning.concept

import shmp.generator.culture.worldview.Meme


interface ReasonConcept {
    val meme: Meme

    val oppositeConcepts: List<ReasonConcept>
    val correspondingConcepts: List<ReasonConcept>
}

abstract class AbstractReasonConcept : ReasonConcept {
    override fun toString() = meme.toString()
}

// Lambdas are used since cross-references between ReasonConcepts lead to code that doesn't compile
abstract class AbstractKotlinSafeReasonConcept(
        private val _oppositeConcepts: () -> List<ReasonConcept>,
        private val _correspondingConcepts: () -> List<ReasonConcept>
) : ReasonConcept {
    override val oppositeConcepts: List<ReasonConcept>
        get() = _oppositeConcepts()
    override val correspondingConcepts: List<ReasonConcept>
        get() = _correspondingConcepts()

    override fun toString() = meme.toString()
}
