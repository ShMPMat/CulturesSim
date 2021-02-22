package shmp.simulation.culture.group.request

import shmp.simulation.culture.group.cultureaspect.concept.AbstractReasonConcept
import shmp.simulation.culture.group.cultureaspect.concept.ReasonConcept
import shmp.simulation.culture.thinking.meaning.MemeSubject


sealed class RequestType: AbstractReasonConcept() {
    override val meme = MemeSubject(this::class.simpleName)
    override val correspondingConcepts: List<ReasonConcept> = emptyList()
    override val oppositeConcepts: List<ReasonConcept> = emptyList()

    object Food : RequestType()
    object Warmth : RequestType()
    object Clothes : RequestType()
    object Shelter : RequestType()

    object Vital : RequestType()
    object Comfort : RequestType()
    object Improvement : RequestType()
    object Trade : RequestType()
    object Luxury : RequestType()
}
