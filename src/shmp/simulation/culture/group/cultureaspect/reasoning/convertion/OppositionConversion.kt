package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.opposes


object OppositionConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        val opposingConversions = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .flatMap { e ->
                    e.subjectConcept.oppositeConcepts.map { o ->
                        e.objectConcept opposes o
                    } + e.objectConcept.correspondingConcepts.map { c ->
                        c opposes e.subjectConcept
                    }
                }

        return opposingConversions.randomElementOrNull()?.let {
            ReasonConversionResult(listOf(it))
        } ?: emptyReasonConversionResult()
    }
}
