package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.opposes
import kotlin.random.Random


object OppositionConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex, random: Random): ReasonConversionResult {
        val opposingConversions = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .flatMap {
                    it.subjectConcept.oppositeConcepts.map { o ->
                        it.objectConcept opposes o
                    }
                }

        return randomElementOrNull(opposingConversions, random)?.let {
            ReasonConversionResult(mutableListOf(it), mutableListOf())
        } ?: emptyReasonAdditionResult()
    }
}
