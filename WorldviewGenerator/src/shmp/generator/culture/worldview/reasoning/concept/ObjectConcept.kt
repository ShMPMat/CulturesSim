package shmp.generator.culture.worldview.reasoning.concept

import shmp.generator.culture.worldview.Meme


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

    object World : ObjectConcept(Meme("World"), { listOf() }, { listOf() })
    object AllLife : ObjectConcept(Meme("AllLife"), { listOf() }, { listOf() })
    object We : ObjectConcept(Meme("We"), { listOf() }, { listOf() })
    object Self : ObjectConcept(Meme("Self"), { listOf() }, { listOf() })

    object Home : ObjectConcept(Meme("Home"), { listOf(Foreign) }, { listOf() })
    object Foreign : ObjectConcept(Meme("Foreign"), { listOf(Home) }, { listOf() })
}