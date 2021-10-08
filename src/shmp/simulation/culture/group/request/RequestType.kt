package shmp.simulation.culture.group.request

import shmp.generator.culture.worldview.reasoning.concept.AbstractReasonConcept
import shmp.generator.culture.worldview.reasoning.concept.ReasonConcept
import shmp.generator.culture.worldview.Meme


sealed class RequestType: AbstractReasonConcept() {
    override val meme = Meme(this::class.simpleName!!)
    override val correspondingConcepts: List<ReasonConcept> = emptyList()
    override val oppositeConcepts: List<ReasonConcept> = emptyList()

    object Food : RequestType()
    object Warmth : RequestType()
    object Clothes : RequestType()
    object Shelter : RequestType()

    object Vital : RequestType()
    object Spiritual : RequestType()
    object Comfort : RequestType()
    object Improvement : RequestType()
    object Trade : RequestType()
    object Luxury : RequestType()
}
