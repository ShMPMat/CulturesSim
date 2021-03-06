package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.equal


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
