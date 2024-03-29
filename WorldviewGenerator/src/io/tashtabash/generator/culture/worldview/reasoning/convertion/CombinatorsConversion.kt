package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.random.singleton.testProbability
import io.tashtabash.generator.culture.worldview.reasoning.EqualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex


object CombinatorsConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateAndChoose {
        state = combinators.mapNotNull { c -> c.takeIf { it.probability.testProbability() } }
                .flatMap { combinator ->
                    complex.equalityReasonings
                            .groupBy { it.objectConcept }
                            .mapNotNull { (concept, related) ->
                                val relatedConcepts = related.map { it.subjectConcept }
                                if (combinator.startConcepts.all { it in relatedConcepts })
                                    EqualityReasoning(concept, combinator.result)
                                else null
                            } + complex.equalityReasonings
                            .groupBy { it.subjectConcept }
                            .mapNotNull { (concept, related) ->
                                val relatedConcepts = related.map { it.objectConcept }
                                if (combinator.startConcepts.all { it in relatedConcepts })
                                    EqualityReasoning(combinator.result, concept)
                                else null
                            }
                }
    }
}
