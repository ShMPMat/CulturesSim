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
) : AbstractReasoning() {
    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): BaseReasoning? =
            BaseReasoning(meme, additionalMemes, conclusions)
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

    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): AssociationReasoning? {
        return AssociationReasoning(
                firstAssociation.substitute(substitutions) ?: return null,
                secondAssociation.substitute(substitutions) ?: return null
        )
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

    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): EqualityReasoning? {
        return EqualityReasoning(
                objectConcept.substitute(substitutions) ?: return null,
                subjectConcept.substitute(substitutions) ?: return null
        )
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
) {
    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): QualityReasoning? {
        return QualityReasoning(
                subjectConcept.substitute(substitutions) ?: return null,
                qualityConcept.substitute(substitutions) ?: return null,
                conceptChange,
                adjectiveConceptChange
        )
    }
}

class OppositionReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept) : BaseReasoning(
        Meme("$objectConcept opposes $subjectConcept"),
        listOf(objectConcept.meme, subjectConcept.meme),
        listOf()
) {
    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): OppositionReasoning? {
        return OppositionReasoning(
                objectConcept.substitute(substitutions) ?: return null,
                subjectConcept.substitute(substitutions) ?: return null
        )
    }
}

class ActionReasoning(val objectConcept: ReasonConcept, val actionConcept: ActionConcept) : BaseReasoning(
        Meme("$objectConcept needs $actionConcept"),
        listOf(objectConcept.meme, actionConcept.meme),
        listOf()
) {
    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): ActionReasoning? {
        return ActionReasoning(
                objectConcept.substitute(substitutions) ?: return null,
                actionConcept.substitute(substitutions)
                        ?.takeIf { it is ActionConcept }
                        as ActionConcept?
                        ?: return null
        )
    }
}

class ExistenceInReasoning(val subjectConcept: ObjectConcept, val surroundingConcept: ReasonConcept) : BaseReasoning(
        Meme("$subjectConcept live in $surroundingConcept"),
        listOf(subjectConcept.meme, surroundingConcept.meme),
        listOf()
) {
    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): ExistenceInReasoning? {
        return ExistenceInReasoning(
                subjectConcept.substitute(substitutions)
                        ?.takeIf { it is ObjectConcept }
                        as ObjectConcept?
                        ?: return null,
                surroundingConcept.substitute(substitutions) ?: return null
        )
    }
}

class ConceptBoxReasoning(val concept: ReasonConcept) : AbstractReasoning() {
    override val meme = concept.meme
    override val additionalMemes = listOf<Meme>()
    override val conclusions = listOf<ReasonConclusion>()

    override fun substitute(substitutions: Map<ReasonConcept, ReasonConcept>): ConceptBoxReasoning? {
        return ConceptBoxReasoning(
                concept.substitute(substitutions) ?: return null
        )
    }
}
