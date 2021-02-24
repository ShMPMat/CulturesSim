package shmp.simulation.culture.group.cultureaspect.reasoning.concept

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


sealed class ObjectConcept(
        override val meme: Meme,
        oppositeConcepts: () -> List<ReasonConcept>,
        correspondingConcepts: () -> List<ReasonConcept>
) : AbstractKotlinBugSafeReasonConcept(oppositeConcepts, correspondingConcepts) {
    open class ArbitraryObjectConcept(objectMeme: Meme) : ObjectConcept(objectMeme, { listOf() }, { listOf() }) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ArbitraryObjectConcept) return false

            if (meme != other.meme) return false

            return true
        }

        override fun hashCode(): Int {
            return meme.hashCode()
        }
    }

    object World : ObjectConcept(MemeSubject("World"), { listOf() }, { listOf() })
    object AllLife : ObjectConcept(MemeSubject("AllLife"), { listOf() }, { listOf() })
    object Self : ObjectConcept(MemeSubject("Self"), { listOf() }, { listOf() })

    object Home : ObjectConcept(MemeSubject("Home"), { listOf(Foreign) }, { listOf() })
    object Foreign : ObjectConcept(MemeSubject("Foreign"), { listOf(Home) }, { listOf() })
}