package shmp.simulation.culture.group.centers.util

import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.simulation.culture.group.centers.StratumCenter
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.associateWith
import shmp.simulation.culture.group.cultureaspect.reasoning.associatesWith
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.equalsAll
import shmp.simulation.culture.group.stratum.AspectStratum
import shmp.simulation.culture.group.stratum.NonAspectStratum
import shmp.simulation.culture.group.stratum.Stratum


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
