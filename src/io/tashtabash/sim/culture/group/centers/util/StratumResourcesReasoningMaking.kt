package io.tashtabash.sim.culture.group.centers.util

import io.tashtabash.generator.culture.worldview.reasoning.EqualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversion
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.convertion.calculateOn
import io.tashtabash.generator.culture.worldview.reasoning.convertion.emptyReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.equals
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.culture.group.stratum.AspectStratum
import io.tashtabash.sim.space.resource.Resource


object StratumResourcesConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.calculateOn<EqualityReasoning> {
        state.mapNotNull { equalityReasoning ->
            if (equalityReasoning.objectConcept !is ArbitraryStratum)
                return@mapNotNull null

            (equalityReasoning.objectConcept as ArbitraryStratum).stratum to equalityReasoning.subjectConcept
        }
                .randomElementOrNull()
                ?.let { (stratum, subjectConcept) ->
                    if (stratum !is AspectStratum)
                        return emptyReasonConversionResult()

                    val resource: Resource = (stratum.aspect.producedResources + listOf(stratum.aspect.resource))
                            .randomElementOrNull()
                            ?: return emptyReasonConversionResult()
                    val resourceConcept = ArbitraryResource(resource)

                    ReasonConversionResult(resourceConcept equals subjectConcept, resourceConcept)
                } ?: emptyReasonConversionResult()
    }
}
