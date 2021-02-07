package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


interface ReasonConcept {
    val meme: Meme

    val oppositeConcepts: List<ReasonConcept>
    val correspondingConcepts: List<ReasonConcept>
}

abstract class AbstractReasonConcept : ReasonConcept {
    override fun toString() = meme.toString()
}

sealed class IdeationalConcept(
        override val meme: Meme,
        override val oppositeConcepts: List<ReasonConcept>,
        override val correspondingConcepts: List<ReasonConcept>
) : AbstractReasonConcept() {
    object Good : IdeationalConcept(MemeSubject("Good"), listOf(Bad), listOf())
    object Bad : IdeationalConcept(MemeSubject("Bad"), listOf(Good), listOf())
    object NoEvaluation : IdeationalConcept(MemeSubject("NoEvaluation"), listOf(Good, Bad), listOf())
    object Uncertainty : IdeationalConcept(MemeSubject("Uncertainty"), listOf(), listOf())

    object Peace : IdeationalConcept(MemeSubject("Peace"), listOf(War, Death), listOf())
    object War : IdeationalConcept(MemeSubject("War"), listOf(Peace), listOf())

    object Expansion : IdeationalConcept(MemeSubject("Expansion"), listOf(Content), listOf())
    object Content : IdeationalConcept(MemeSubject("Content"), listOf(Expansion), listOf())

    object Consolidation : IdeationalConcept(MemeSubject("Consolidation"), listOf(Freedom), listOf())
    object Freedom : IdeationalConcept(MemeSubject("Freedom"), listOf(Consolidation), listOf())

    object Creation : IdeationalConcept(MemeSubject("Creation"), listOf(Destruction), listOf())
    object Destruction : IdeationalConcept(MemeSubject("Destruction"), listOf(Creation), listOf())

    object Hardship : IdeationalConcept(MemeSubject("Hardship"), listOf(Comfort), listOf())
    object Comfort : IdeationalConcept(MemeSubject("Comfort"), listOf(Hardship), listOf())

    object Importance : IdeationalConcept(MemeSubject("Importance"), listOf(Unimportance), listOf())
    object Unimportance : IdeationalConcept(MemeSubject("Unimportance"), listOf(Importance), listOf())

    object Change : IdeationalConcept(MemeSubject("Change"), listOf(Permanence), listOf())
    object Permanence : IdeationalConcept(MemeSubject("Permanence"), listOf(Change), listOf())

    object Life : IdeationalConcept(MemeSubject("Life"), listOf(Death), listOf())
    object Death : IdeationalConcept(MemeSubject("Death"), listOf(Life), listOf())

    object Uniqueness : IdeationalConcept(MemeSubject("Uniqueness"), listOf(Commonness), listOf())
    object Commonness : IdeationalConcept(MemeSubject("Commonness"), listOf(Uniqueness), listOf())

    object Simpleness : IdeationalConcept(MemeSubject("Simpleness"), listOf(Complexity), listOf())
    object Complexity : IdeationalConcept(MemeSubject("Complexity"), listOf(Simpleness), listOf())
}

sealed class ObjectConcept(
        override val meme: Meme,
        override val oppositeConcepts: List<ReasonConcept>,
        override val correspondingConcepts: List<ReasonConcept>
) : AbstractReasonConcept() {
    class ArbitraryObject(val objectMeme: Meme) : ObjectConcept(objectMeme, listOf(), listOf()) {
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
}

class DeterminedConcept(val objectConcept: ObjectConcept, val ideationalConcept: IdeationalConcept) : ReasonConcept {
    override val meme = MemeSubject("$objectConcept\'s $ideationalConcept")
    override val oppositeConcepts = objectConcept.oppositeConcepts + ideationalConcept.oppositeConcepts
    override val correspondingConcepts = objectConcept.correspondingConcepts + ideationalConcept.correspondingConcepts
}
