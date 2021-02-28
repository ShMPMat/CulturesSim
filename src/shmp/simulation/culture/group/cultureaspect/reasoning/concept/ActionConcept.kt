package shmp.simulation.culture.group.cultureaspect.reasoning.concept

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


sealed class ActionConcept(
        override val meme: Meme,
        oppositeConcepts: () -> List<ReasonConcept>,
        correspondingConcepts: () -> List<ReasonConcept>,
        val ideationalConcept: IdeationalConcept
) : AbstractKotlinBugSafeReasonConcept(oppositeConcepts, correspondingConcepts) {
    open class ArbitraryActionConcept(objectMeme: Meme) : ActionConcept(objectMeme, { listOf() }, { listOf() }, Work) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ArbitraryActionConcept) return false

            if (meme != other.meme) return false

            return true
        }

        override fun hashCode(): Int {
            return meme.hashCode()
        }
    }

    object Protect: ActionConcept(MemeSubject("Protect"), { listOf(Abandon) }, { listOf() }, Defence)
    object Abandon: ActionConcept(MemeSubject("Abandon"), { listOf(Protect) }, { listOf() }, Abandonment)

    object Cherish: ActionConcept(MemeSubject("Cherish"), { listOf(Ignore) }, { listOf() }, Importance)
    object Ignore: ActionConcept(MemeSubject("Ignore"), { listOf(Cherish) }, { listOf() }, Unimportance)
}
