package shmp.simulation.culture.group.centers.util

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.equals
import shmp.simulation.culture.group.stratum.AspectStratum
import shmp.simulation.space.resource.Resource


object StratumResourcesConversion : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) = complex.reasonings
            .filterIsInstance<EqualityReasoning>()
            .mapNotNull { equalityReasoning ->
                if (equalityReasoning.objectConcept !is ArbitraryStratum)
                    return@mapNotNull null

                equalityReasoning.objectConcept.stratum to equalityReasoning.subjectConcept
            }
            .randomElementOrNull()
            ?.let { (stratum, subjectConcept) ->
                if (stratum !is AspectStratum)
                    return emptyReasonConversionResult()

                val resource: Resource = (stratum.aspect.producedResources + listOf(stratum.aspect.resource))
                        .randomElementOrNull()
                        ?: return emptyReasonConversionResult()
                val resourceConcept = ArbitraryResource(resource.copy())

                ReasonConversionResult(resourceConcept equals subjectConcept, resourceConcept)
            } ?: emptyReasonConversionResult()
}
