package shmp.simulation.culture.group.centers.util

import shmp.random.singleton.randomElementOrNull
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.convertion.ReasonConversion
import shmp.generator.culture.worldview.reasoning.convertion.ReasonConversionResult
import shmp.generator.culture.worldview.reasoning.convertion.emptyReasonConversionResult
import shmp.generator.culture.worldview.reasoning.equals
import shmp.simulation.culture.group.stratum.AspectStratum
import shmp.simulation.space.resource.Resource


object StratumResourcesConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.reasonings
            .filterIsInstance<EqualityReasoning>()
            .mapNotNull { equalityReasoning ->
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
