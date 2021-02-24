package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.equals


object CorrespondingConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        val correspondingConversions = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .flatMap {
                    it.subjectConcept.correspondingConcepts.map { c ->
                        it.objectConcept equals c
                    }
                }

        return correspondingConversions.randomElementOrNull()?.let {
            ReasonConversionResult(mutableListOf(it), mutableListOf())
        } ?: emptyReasonAdditionResult()
    }
}
