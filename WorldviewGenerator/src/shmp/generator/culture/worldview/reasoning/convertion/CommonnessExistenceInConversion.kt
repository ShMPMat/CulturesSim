package shmp.generator.culture.worldview.reasoning.convertion

import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElementOrNull
import shmp.generator.culture.worldview.reasoning.*
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept
import shmp.generator.culture.worldview.reasoning.concept.ObjectConcept


object CommonnessExistenceInConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) =
            0.5.chanceOf<ReasonConversionResult> {
                complex.reasonings
                        .filterIsInstance<ExistenceInReasoning>()
                        .randomElementOrNull()
                        .toConversionResult()
            } ?: run {
                complex.reasonings
                        .filterIsInstance<EqualityReasoning>()
                        .mapNotNull {
                            if (it.subjectConcept == IdeationalConcept.Commonness)
                                ObjectConcept.We livesIn it.objectConcept
                            else null
                        }
                        .randomElementOrNull()
                        .toConversionResult()
            }
}
