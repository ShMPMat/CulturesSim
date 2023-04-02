package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.generator.culture.worldview.reasoning.EqualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.concept.ActionConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.ActionConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.needs
import io.tashtabash.utils.without


sealed class ActionConversion(
        val ideationalConcepts: List<IdeationalConcept>,
        val actionConcepts: List<ActionConcept>
) : ReasonConversion {
    constructor(ideationalConcept: IdeationalConcept, actionConcept: ActionConcept) :
            this(listOf(ideationalConcept), listOf(actionConcept))

    override fun makeConversion(complex: ReasonComplex) = complex.calculateOn<EqualityReasoning> {
        filter { it.subjectConcept in ideationalConcepts }

        withRandom {
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
