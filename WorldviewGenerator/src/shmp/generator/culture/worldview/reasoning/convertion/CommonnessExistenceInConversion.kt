package shmp.generator.culture.worldview.reasoning.convertion

import shmp.random.singleton.chanceOf
import shmp.generator.culture.worldview.reasoning.*
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept
import shmp.generator.culture.worldview.reasoning.concept.ObjectConcept


object CommonnessExistenceInConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateAndChoose {
        0.5.chanceOf<Unit> {
            filter<ExistenceInReasoning>()
        } ?: run {
            mapInstanceNotNull { r: EqualityReasoning ->
                if (r.subjectConcept == IdeationalConcept.Commonness)
                    ObjectConcept.We livesIn r.objectConcept
                else null
            }
        }
    }
}
