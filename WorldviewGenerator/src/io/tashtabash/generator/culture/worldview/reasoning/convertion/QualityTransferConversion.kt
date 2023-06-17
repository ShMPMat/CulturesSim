package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.generator.culture.worldview.reasoning.EqualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.QualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.Reasoning


object QualityTransferConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateOn<Reasoning> {
        val equalityReasonings = state.filterIsInstance<EqualityReasoning>()

        val res = state.filterIsInstance<QualityReasoning>()
                .map { q -> q to equalityReasonings.filter { it.contains(q.subjectConcept) } }
                .filter { it.second.isNotEmpty() }
                .randomElementOrNull()
                ?.let { (q, es) ->
                    val equalityReasoning = es.randomElementOrNull()
                            ?: return@let null
                    val subject = if (q.subjectConcept == equalityReasoning.subjectConcept)
                        equalityReasoning.objectConcept
                    else equalityReasoning.subjectConcept

                    QualityReasoning(subject, q.qualityConcept, q.conceptChange, q.adjectiveConceptChange)
                }
                .toConversionResult()

        if (res.isNotEmpty()) {
            val j = 0
        }

        res
    }
}
