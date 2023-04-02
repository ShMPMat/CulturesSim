package io.tashtabash.simulation.culture.group.centers.util

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.random.singleton.testProbability
import io.tashtabash.simulation.culture.group.centers.StratumCenter
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.associateWith
import io.tashtabash.generator.culture.worldview.reasoning.associatesWith
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversion
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.convertion.emptyReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.equalsAll
import io.tashtabash.simulation.culture.group.stratum.AspectStratum
import io.tashtabash.simulation.culture.group.stratum.NonAspectStratum
import io.tashtabash.simulation.culture.group.stratum.Stratum


class StratumConversion(private val stratumCenter: StratumCenter) : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        return if (0.5.testProbability()) makeStratumReason(
                { it.importance > 0 },
                { it.importance.toDouble() },
                listOf(Importance, Good)
        ) else makeStratumReason(
                { it.importance <= 0 },
                { 1 - it.importance.toDouble() },
                listOf(Unimportance, Bad)
        )
    }

    private fun makeStratumReason(
            filter: (Stratum) -> Boolean,
            probability: (Stratum) -> Double,
            appropriateConcepts: List<ReasonConcept>
    ): ReasonConversionResult {
        val stratum = stratumCenter.strata
                .filter(filter)
                .randomElementOrNull(probability)
                ?: return emptyReasonConversionResult()

        val concept = ArbitraryStratum(stratum)
        val reasoning = (concept equalsAll appropriateConcepts).randomElement()

        var additionalResults = emptyReasonConversionResult()

        if (stratum is AspectStratum) {
            val aspect = stratum.aspect
            additionalResults += ReasonConversionResult(ArbitraryAspect(aspect) associatesWith concept)

            val allResources = aspect.getAssociatedResources()
            val associations = allResources.map { ArbitraryResource(it) } associateWith listOf(concept)
            additionalResults += ReasonConversionResult(associations)
        } else if  (stratum is NonAspectStratum) {
            stratum.aspect?.let { aspect ->
                additionalResults += ReasonConversionResult(ArbitraryAspect(aspect) associatesWith concept)

                val allResources = aspect.getAssociatedResources()
                val associations = allResources.map { ArbitraryResource(it) } associateWith listOf(concept)
                additionalResults += ReasonConversionResult(associations)
            }
        }

        return ReasonConversionResult(reasoning, concept) + additionalResults
    }
}
