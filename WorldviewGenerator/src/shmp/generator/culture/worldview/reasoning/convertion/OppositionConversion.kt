package shmp.generator.culture.worldview.reasoning.convertion

import shmp.random.singleton.randomElementOrNull
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.opposes


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
