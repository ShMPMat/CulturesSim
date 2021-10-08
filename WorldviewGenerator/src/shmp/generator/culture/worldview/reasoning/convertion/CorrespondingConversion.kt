package shmp.generator.culture.worldview.reasoning.convertion

import shmp.random.singleton.randomElementOrNull
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.equal


object CorrespondingConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        val correspondingConversions = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .flatMap { it.objectConcept.correspondingConcepts equal it.subjectConcept.correspondingConcepts }

        return correspondingConversions.randomElementOrNull()?.let {
            ReasonConversionResult(listOf(it))
        } ?: emptyReasonConversionResult()
    }
}
