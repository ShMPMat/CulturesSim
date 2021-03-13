package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.testProbability
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex


object CombinatorsConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
//        return combinators
//                .mapNotNull { c -> c.takeIf { it.probability.testProbability() } }
//                .flatMap { combinator ->
//                    complex.equalityReasonings.app
//                }
        return emptyReasonConversionResult()
    }
}
