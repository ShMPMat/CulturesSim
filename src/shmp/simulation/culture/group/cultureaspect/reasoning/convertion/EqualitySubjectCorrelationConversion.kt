package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.randomSublist
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex


object EqualitySubjectCorrelationConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        val opposingConversions = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .groupBy { it.objectConcept }
                .filter { it.value.size > 1 }
                .map { it.value }
                .randomElementOrNull()
                ?.map { it.subjectConcept }
                ?: return emptyReasonConversionResult()

        val (subj, obj) = randomSublist(opposingConversions, RandomSingleton.random, 2, 3)

        return ReasonConversionResult(listOf(EqualityReasoning(subj, obj)))
    }
}
