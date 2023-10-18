package io.tashtabash.generator.culture.worldview.reasoning.concept

import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import io.tashtabash.generator.culture.worldview.Meme


sealed class ActionConcept(
        override val meme: Meme,
        oppositeConcepts: () -> List<ReasonConcept>,
        correspondingConcepts: () -> List<ReasonConcept>,
        val ideationalConcept: IdeationalConcept
) : AbstractKotlinSafeReasonConcept(oppositeConcepts, correspondingConcepts) {
    open class ArbitraryActionConcept(objectMeme: Meme) : ActionConcept(objectMeme, { listOf() }, { listOf() }, Work) {
        override fun copy() = ArbitraryActionConcept(meme)

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

    override fun copy() = this

    object Protect: ActionConcept(Meme("Protect"), { listOf(Abandon) }, { listOf() }, Defence)
    object Abandon: ActionConcept(Meme("Abandon"), { listOf(Protect) }, { listOf() }, Abandonment)

    object Cherish: ActionConcept(Meme("Cherish"), { listOf(Ignore) }, { listOf() }, Importance)
    object Ignore: ActionConcept(Meme("Ignore"), { listOf(Cherish) }, { listOf() }, Unimportance)

    object Fear: ActionConcept(Meme("Fear"), { listOf(Face) }, { listOf() }, IdeationalConcept.Fear)
    object Face: ActionConcept(Meme("Face"), { listOf(Fear) }, { listOf() }, Courage)

    object BeCloser: ActionConcept(Meme("Be closer to"), { listOf(WardOff) }, { listOf() }, Spirituality)
    object WardOff: ActionConcept(Meme("Ward off"), { listOf(BeCloser) }, { listOf() }, Danger)
}
