package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.opposes


object OppositionConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        val opposingConversions = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .flatMap {
                    it.subjectConcept.oppositeConcepts.map { o ->
                        it.objectConcept opposes o
                    }
                }

        return opposingConversions.randomElementOrNull()?.let {
            ReasonConversionResult(mutableListOf(it), mutableListOf())
        } ?: emptyReasonAdditionResult()
    }
}
