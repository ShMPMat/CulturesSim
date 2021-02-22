package shmp.simulation.culture.group.cultureaspect.concept

import shmp.simulation.culture.group.cultureaspect.concept.IdeationalConcept.*
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


sealed class ActionConcept(
        override val meme: Meme,
        override val oppositeConcepts: List<ReasonConcept>,
        override val correspondingConcepts: List<ReasonConcept>,
        val ideationalConcept: IdeationalConcept
) : AbstractReasonConcept() {
    object Protect: ActionConcept(MemeSubject("Protect"), listOf(Abandon), listOf(), Defence)
    object Abandon: ActionConcept(MemeSubject("Abandon"), listOf(Protect), listOf(), Abandonment)

    object Cherish: ActionConcept(MemeSubject("Cherish"), listOf(Ignore), listOf(), Importance)
    object Ignore: ActionConcept(MemeSubject("Ignore"), listOf(Cherish), listOf(), Unimportance)
}
