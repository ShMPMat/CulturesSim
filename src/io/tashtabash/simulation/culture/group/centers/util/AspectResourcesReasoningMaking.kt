package io.tashtabash.simulation.culture.group.centers.util

import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.generator.culture.worldview.reasoning.EqualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversion
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.convertion.calculateOn
import io.tashtabash.generator.culture.worldview.reasoning.convertion.emptyReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.equals


object AspectResourcesConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateOn<EqualityReasoning> {
        state.mapNotNull { equalityReasoning ->
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
}
