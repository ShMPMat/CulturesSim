package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.randomElement
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.needs
import shmp.utils.without


sealed class ActionConversion(
        val ideationalConcepts: List<IdeationalConcept>,
        val actionConcepts: List<ActionConcept>
) : ReasonConversion {
    constructor(ideationalConcept: IdeationalConcept, actionConcept: ActionConcept) :
            this(listOf(ideationalConcept), listOf(actionConcept))

    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        val concept = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .filter { it.subjectConcept in ideationalConcepts }
                .map { it.subjectConcept }
                .randomOrNull()
                ?: return emptyReasonConversionResult()

        return ReasonConversionResult(concept needs actionConcepts.randomElement())
    }

    object PositiveDriveConversion : ActionConversion(listOf(Importance), listOf(Protect, Cherish))

    class CorrespondingIdealActionConversion(
            actionConcept: ActionConcept
    ) : ActionConversion(actionConcept.ideationalConcept, actionConcept)
}

val allCorrespondingIdealActionConversions = ActionConcept::class.sealedSubclasses
        .without(ArbitraryActionConcept::class)
        .mapNotNull { it.objectInstance }
        .map { ActionConversion.CorrespondingIdealActionConversion(it) }
