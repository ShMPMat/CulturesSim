package shmp.simulation.culture.group.cultureaspect.concept

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


sealed class ObjectConcept(
        override val meme: Meme,
        override val oppositeConcepts: List<ReasonConcept>,
        override val correspondingConcepts: List<ReasonConcept>
) : AbstractReasonConcept() {
    open class ArbitraryObject(val objectMeme: Meme) : ObjectConcept(objectMeme, listOf(), listOf()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ArbitraryObject) return false

            if (objectMeme != other.objectMeme) return false

            return true
        }

        override fun hashCode(): Int {
            return objectMeme.hashCode()
        }
    }

    object World : ObjectConcept(MemeSubject("World"), listOf(), listOf())
    object AllLife : ObjectConcept(MemeSubject("AllLife"), listOf(), listOf())
    object Self : ObjectConcept(MemeSubject("Self"), listOf(), listOf())

    object Home : ObjectConcept(MemeSubject("Home"), listOf(Foreign), listOf())
    object Foreign : ObjectConcept(MemeSubject("Foreign"), listOf(Home), listOf())

}
