package io.tashtabash.simulation.culture.group.centers.util

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.random.singleton.testProbability
import io.tashtabash.simulation.culture.aspect.Aspect
import io.tashtabash.simulation.culture.group.centers.AspectCenter
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.associateWith
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversion
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.convertion.emptyReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.equalsAll


class AspectConversion(private val aspectCenter: AspectCenter) : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        return if (0.5.testProbability()) makeAspectReason(
                { it.usefulness > 0 },
                { it.usefulness.toDouble() },
                listOf(Importance, Good)
        ) else makeAspectReason(
                { it.usefulness <= 0 },
                { 1 - it.usefulness.toDouble() },
                listOf(Unimportance, Bad)
        )
    }

    private fun makeAspectReason(
            filter: (Aspect) -> Boolean,
            probability: (Aspect) -> Double,
            appropriateConcepts: List<ReasonConcept>
    ): ReasonConversionResult {
        val aspect = aspectCenter.aspectPool.all
                .filter(filter)
                .sortedBy { it.usefulness }
                .randomElementOrNull(probability)
                ?: return emptyReasonConversionResult()

        val concept = ArbitraryAspect(aspect)
        val reasoning = (concept equalsAll appropriateConcepts).randomElement()
        val allResources = aspect.getAssociatedResources()
        val associations = allResources.map { ArbitraryResource(it) } associateWith listOf(concept)

        return ReasonConversionResult(reasoning, concept) + ReasonConversionResult(associations)
    }
}

internal fun Aspect.getAssociatedResources() = producedResources //+
// aspect.dependencies.map.values.flatten().map { it. }//TODO dependencies
