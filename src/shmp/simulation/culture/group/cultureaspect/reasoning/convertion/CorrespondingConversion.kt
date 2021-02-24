package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.equals
import shmp.simulation.culture.group.cultureaspect.reasoning.opposes
import kotlin.random.Random


object CorrespondingConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex, random: Random): ReasonConversionResult {
        val correspondingConversions = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .flatMap {
                    it.subjectConcept.correspondingConcepts.map { c ->
                        it.objectConcept equals c
                    }
                }

        return randomElementOrNull(correspondingConversions, random)?.let {
            ReasonConversionResult(mutableListOf(it), mutableListOf())
        } ?: emptyReasonAdditionResult()
    }
}
