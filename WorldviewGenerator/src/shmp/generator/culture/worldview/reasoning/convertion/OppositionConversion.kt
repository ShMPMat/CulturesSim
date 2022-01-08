package shmp.generator.culture.worldview.reasoning.convertion

import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.oppose


object OppositionConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateAndChoose {
        flatMapInstance { r: EqualityReasoning ->
            (r.objectConcept oppose r.subjectConcept.oppositeConcepts) +
                    (r.objectConcept.correspondingConcepts oppose r.subjectConcept)
        }
    }
}
