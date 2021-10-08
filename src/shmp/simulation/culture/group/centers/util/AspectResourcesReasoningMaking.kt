package shmp.simulation.culture.group.centers.util

import shmp.random.singleton.randomElementOrNull
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.convertion.ReasonConversion
import shmp.generator.culture.worldview.reasoning.convertion.ReasonConversionResult
import shmp.generator.culture.worldview.reasoning.convertion.emptyReasonConversionResult
import shmp.generator.culture.worldview.reasoning.equals


object AspectResourcesConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.reasonings
            .filterIsInstance<EqualityReasoning>()
            .mapNotNull { equalityReasoning ->
                if (equalityReasoning.objectConcept !is ArbitraryAspect)
                    return@mapNotNull null

                (equalityReasoning.objectConcept as ArbitraryAspect).aspect to equalityReasoning.subjectConcept
            }
            .randomElementOrNull()
            ?.let { (aspect, subjectConcept) ->
                val resource = aspect.producedResources.randomElementOrNull()
                        ?: return emptyReasonConversionResult()
                val resourceConcept = ArbitraryResource(resource)

                ReasonConversionResult(resourceConcept equals subjectConcept, resourceConcept)
            } ?: emptyReasonConversionResult()
}
