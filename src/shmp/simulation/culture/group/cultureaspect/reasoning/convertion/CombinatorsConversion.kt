package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex


object CombinatorsConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        return combinators
                .mapNotNull { c -> c.takeIf { it.probability.testProbability() } }
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
                }.randomElementOrNull()
                ?.toConversionResult()
                ?: emptyReasonConversionResult()
    }
}
