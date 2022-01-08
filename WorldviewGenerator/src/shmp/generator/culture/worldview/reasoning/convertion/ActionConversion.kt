package shmp.generator.culture.worldview.reasoning.convertion

import shmp.random.singleton.randomElement
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.concept.ActionConcept
import shmp.generator.culture.worldview.reasoning.concept.ActionConcept.*
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import shmp.generator.culture.worldview.reasoning.needs
import shmp.utils.without


sealed class ActionConversion(
        val ideationalConcepts: List<IdeationalConcept>,
        val actionConcepts: List<ActionConcept>
) : ReasonConversion {
    constructor(ideationalConcept: IdeationalConcept, actionConcept: ActionConcept) :
            this(listOf(ideationalConcept), listOf(actionConcept))

    override fun makeConversion(complex: ReasonComplex) = complex.calculate {
        filterInstances<EqualityReasoning> { it.subjectConcept in ideationalConcepts }

        withRandom<EqualityReasoning> {
            ReasonConversionResult(it.objectConcept needs actionConcepts.randomElement())
        }
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
