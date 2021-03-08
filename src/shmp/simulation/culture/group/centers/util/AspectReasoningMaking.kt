package shmp.simulation.culture.group.centers.util

import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.group.centers.AspectCenter
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.allEqualsAll
import shmp.simulation.culture.group.cultureaspect.reasoning.equalsAll


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

        return ReasonConversionResult(reasoning, concept)
    }
}
