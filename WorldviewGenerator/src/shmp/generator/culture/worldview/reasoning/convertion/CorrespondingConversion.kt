package shmp.generator.culture.worldview.reasoning.convertion

import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.equal


object CorrespondingConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateAndChoose {
        flatMap { r: EqualityReasoning ->
            r.objectConcept.correspondingConcepts equal r.subjectConcept.correspondingConcepts
        }
    }
}
