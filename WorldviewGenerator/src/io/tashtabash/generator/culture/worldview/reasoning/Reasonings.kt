package io.tashtabash.generator.culture.worldview.reasoning

import io.tashtabash.generator.culture.worldview.reasoning.concept.ActionConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.generator.culture.worldview.reasoning.concept.conceptToAdjectiveString


open class BaseReasoning(
        override val meme: Meme,
        override val additionalMemes: List<Meme>,
        override val conclusions: List<ReasonConclusion>
) : AbstractReasoning()

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

class EqualityReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept) : BaseReasoning(
        Meme("$objectConcept represents $subjectConcept"),
        listOf(objectConcept.meme, subjectConcept.meme),
        listOf()
) {
    val isOppositions = any { it in subjectConcept.oppositeConcepts || it in objectConcept.oppositeConcepts }

    fun toList() = listOf(objectConcept, subjectConcept)

    fun any(predicate: (ReasonConcept) -> Boolean) = predicate(objectConcept) || predicate(subjectConcept)
    fun all(predicate: (ReasonConcept) -> Boolean) = predicate(objectConcept) && predicate(subjectConcept)

    fun contains(reasonConcept: ReasonConcept): Boolean =
            reasonConcept == objectConcept || reasonConcept == subjectConcept

    fun <T> applyToBoth(action: (ReasonConcept, ReasonConcept) -> T?): List<T> {
        return listOfNotNull(action(objectConcept, subjectConcept), action(subjectConcept, objectConcept))
    }
}

class QualityReasoning(
        val subjectConcept: ReasonConcept,
        val qualityConcept: ReasonConcept,
        val conceptChange: Double,
        val adjectiveConceptChange: Double
) : BaseReasoning(
        Meme("${subjectConcept.meme} is ${conceptToAdjectiveString(qualityConcept)}"),
        listOf(subjectConcept.meme, qualityConcept.meme),
        listOf(subjectConcept.toConclusion(conceptChange), qualityConcept.toConclusion(adjectiveConceptChange))
)

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
