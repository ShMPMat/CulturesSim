package shmp.generator.culture.worldview.reasoning.convertion

import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.opposes


object OppositionConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateAndChoose {
        flatMap { r: EqualityReasoning ->
            r.subjectConcept.oppositeConcepts.map { o -> r.objectConcept opposes o } +
                    r.objectConcept.correspondingConcepts.map { c -> c opposes r.subjectConcept }
        }
    }
}
