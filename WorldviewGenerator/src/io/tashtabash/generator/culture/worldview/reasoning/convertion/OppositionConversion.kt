package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.generator.culture.worldview.reasoning.EqualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.oppose


object OppositionConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateAndChoose {
        flatMapInstance { r: EqualityReasoning ->
            (r.objectConcept oppose r.subjectConcept.oppositeConcepts) +
                    (r.objectConcept.correspondingConcepts oppose r.subjectConcept)
        }
    }
}
