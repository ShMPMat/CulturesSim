package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept


object CommonnessExistenceInConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) =
            0.5.chanceOf<ReasonConversionResult> {
                complex.reasonings
                        .filterIsInstance<ExistenceInReasoning>()
                        .randomElementOrNull()
                        ?.toConversionResult()
                        ?: emptyReasonConversionResult()
            } ?: run {
                complex.reasonings
                        .filterIsInstance<EqualityReasoning>()
                        .mapNotNull {
                            if (it.subjectConcept == IdeationalConcept.Commonness)
                                ObjectConcept.We livesIn it.objectConcept
                            else null
                        }
                        .randomElementOrNull()
                        ?.toConversionResult()
                        ?: emptyReasonConversionResult()
            }
}
