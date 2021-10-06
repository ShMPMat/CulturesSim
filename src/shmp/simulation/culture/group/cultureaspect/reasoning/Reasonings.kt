package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.generator.culture.worldview.Meme


open class BaseReasoning(
        override val meme: Meme,
        override val additionalMemes: List<Meme>,
        override val conclusions: List<ReasonConclusion>
) : AbstractReasoning()

class EqualityReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept) : BaseReasoning(
        Meme("$objectConcept represents $subjectConcept"),
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

class AssociationReasoning(val firstAssociation: ReasonConcept, val secondAssociation: ReasonConcept) : BaseReasoning(
        Meme("$firstAssociation associates with $secondAssociation"),
        listOf(firstAssociation.meme, secondAssociation.meme),
        listOf()
) {
    val isOppositions = any { it in firstAssociation.oppositeConcepts || it in secondAssociation.oppositeConcepts }

    fun toList() = listOf(firstAssociation, secondAssociation)

    fun any(predicate: (ReasonConcept) -> Boolean) = predicate(firstAssociation) || predicate(secondAssociation)
    fun all(predicate: (ReasonConcept) -> Boolean) = predicate(firstAssociation) && predicate(secondAssociation)

    fun <T> applyToBoth(action: (ReasonConcept, ReasonConcept) -> T?): List<T> {
        return listOf(action(firstAssociation, secondAssociation), action(secondAssociation, firstAssociation)).mapNotNull { it }
    }
}

class OppositionReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept) : BaseReasoning(
        Meme("$objectConcept opposes $subjectConcept"),
        listOf(objectConcept.meme, subjectConcept.meme),
        listOf()
)

class ActionReasoning(val objectConcept: ReasonConcept, val actionConcept: ActionConcept) : BaseReasoning(
        Meme("$objectConcept needs $actionConcept"),
        listOf(objectConcept.meme, actionConcept.meme),
        listOf()
)

class ExistenceInReasoning(val subjectConcept: ObjectConcept, val surroundingConcept: ReasonConcept) : BaseReasoning(
        Meme("$subjectConcept live in $surroundingConcept"),
        listOf(subjectConcept.meme, surroundingConcept.meme),
        listOf()
)

class ConceptBoxReasoning(val concept: ReasonConcept) : AbstractReasoning() {
    override val meme = concept.meme
    override val additionalMemes = listOf<Meme>()
    override val conclusions = listOf<ReasonConclusion>()
}
