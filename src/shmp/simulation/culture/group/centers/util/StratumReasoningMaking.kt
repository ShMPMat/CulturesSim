package shmp.simulation.culture.group.centers.util

import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.group.centers.AspectCenter
import shmp.simulation.culture.group.centers.StratumCenter
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.equal
import shmp.simulation.culture.group.stratum.Stratum


class StratumConversion(private val stratumCenter: StratumCenter) : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        return if (0.5.testProbability()) makeAspectReason(
                { it.importance > 0 },
                { it.importance.toDouble() },
                listOf(Importance, Good)
        ) else makeAspectReason(
                { it.importance <= 0 },
                { 1 - it.importance.toDouble() },
                listOf(Unimportance, Bad)
        )
    }

    private fun makeAspectReason(
            filter: (Stratum) -> Boolean,
            probability: (Stratum) -> Double,
            appropriateConcepts: List<ReasonConcept>
    ): ReasonConversionResult {
        val stratum = stratumCenter.strata
                .filter(filter)
                .randomElementOrNull(probability)
                ?: return emptyReasonConversionResult()

        val concept = ArbitraryStratum(stratum)
        val reasoning = (listOf(concept) equal appropriateConcepts).randomElement()

        return ReasonConversionResult(reasoning, concept)
    }
}
