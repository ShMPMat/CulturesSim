package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


open class BaseReasoning(
        override val meme: Meme,
        override val additionalMemes: List<Meme>,
        override val conclusions: List<ReasonConclusion>
) : AbstractReasoning()

class EqualityReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept) : BaseReasoning(
        MemeSubject("$objectConcept represents $subjectConcept"),
        listOf(objectConcept.meme, subjectConcept.meme),
        listOf()
) {
    val isOppositions = any { it in subjectConcept.oppositeConcepts || it in objectConcept.oppositeConcepts }

    fun toList() = listOf(objectConcept, subjectConcept)

    fun any(predicate: (ReasonConcept) -> Boolean) = predicate(objectConcept) || predicate(subjectConcept)
    fun all(predicate: (ReasonConcept) -> Boolean) = predicate(objectConcept) && predicate(subjectConcept)

    fun <T> applyToBoth(action: (ReasonConcept, ReasonConcept) -> T?): List<T> {
        return listOf(action(objectConcept, subjectConcept), action(subjectConcept, objectConcept)).mapNotNull { it }
    }
}

class OppositionReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept) : BaseReasoning(
        MemeSubject("$objectConcept opposes $subjectConcept"),
        listOf(objectConcept.meme, subjectConcept.meme),
        listOf()
)

class ActionReasoning(val objectConcept: ReasonConcept, val actionConcept: ActionConcept) : BaseReasoning(
        MemeSubject("$objectConcept needs $actionConcept"),
        listOf(objectConcept.meme, actionConcept.meme),
        listOf()
)

class ExistenceInReasoning(val subjectConcept: ObjectConcept, val surroundingConcept: ReasonConcept) : BaseReasoning(
        MemeSubject("$subjectConcept live in $surroundingConcept"),
        listOf(subjectConcept.meme, surroundingConcept.meme),
        listOf()
)

class ConceptBoxReasoning(val concept: ReasonConcept) : AbstractReasoning() {
    override val meme = concept.meme
    override val additionalMemes = listOf<Meme>()
    override val conclusions = listOf<ReasonConclusion>()
}
