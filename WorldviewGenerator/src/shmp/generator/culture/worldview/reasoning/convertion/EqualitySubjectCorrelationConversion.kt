package shmp.generator.culture.worldview.reasoning.convertion

import shmp.random.randomSublist
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElementOrNull
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex


object EqualitySubjectCorrelationConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateOn<EqualityReasoning> {
        state = state.groupBy { it.objectConcept }
                .filter { it.value.size > 1 }
                .map { it.value }
                .randomElementOrNull()
                ?: emptyList()

        withRandom {
            val (subj, obj) = randomSublist(state.map { it.subjectConcept }, RandomSingleton.random, 2, 3)

            EqualityReasoning(subj, obj).toConversionResult()
        }
    }
}
