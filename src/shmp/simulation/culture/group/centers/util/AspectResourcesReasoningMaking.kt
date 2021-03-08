package shmp.simulation.culture.group.centers.util

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.equals


object AspectResourcesConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.reasonings
            .filterIsInstance<EqualityReasoning>()
            .mapNotNull { equalityReasoning ->
                if (equalityReasoning.objectConcept !is ArbitraryAspect)
                    return@mapNotNull null

                equalityReasoning.objectConcept.aspect to equalityReasoning.subjectConcept
            }
            .randomElementOrNull()
            ?.let { (aspect, subjectConcept) ->
                val resource = aspect.producedResources.randomElementOrNull()
                        ?: return emptyReasonConversionResult()
                val resourceConcept = ArbitraryResource(resource.copy())

                ReasonConversionResult(resourceConcept equals subjectConcept, resourceConcept)
            } ?: emptyReasonConversionResult()
}
