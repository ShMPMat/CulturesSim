package shmp.simulation.culture.group.centers.util

import shmp.random.SampleSpaceObject
import shmp.random.randomElementOrNull
import shmp.random.singleton.*
import shmp.simulation.CulturesController
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.centers.util.ReasoningRandom.*
import shmp.simulation.culture.group.cultureaspect.reasoning.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.request.RequestPool
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.ResultStatus
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.Taker
import shmp.utils.MovingAverage
import kotlin.math.pow


class MemoryConversion(private val memoryCenter: MemoryCenter) : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex) =
            takeOutCommonReasonings(memoryCenter)
}


private enum class ReasoningRandom(override val probability: Double) : SampleSpaceObject {
    Requests(1.0),
    MemoryResources(1.0)
}

private fun takeOutCommonReasonings(memoryCenter: MemoryCenter): ReasonConversionResult =
        when (values().randomElement()) {
            Requests -> takeOutRequest(memoryCenter.turnRequests)
            MemoryResources -> takeOutResourceTraction(memoryCenter.resourceTraction)
        }

private fun takeOutResourceTraction(resourceTraction: Map<Resource, MovingAverage>): ReasonConversionResult {
    return 0.5.chanceOf<ReasonConversionResult> {
        val commonResource = resourceTraction.entries
                .sortedBy { it.value }
                .randomElementOrNull { it.value.value.value }
                ?.key
                ?: return emptyReasonConversionResult()
        val resourceConcept = ArbitraryResource(commonResource)

        commonResource.getAdditionalConcepts() + (0.5.chanceOf<ReasonConversionResult> {
            ReasonConversionResult(resourceConcept equals Commonness, resourceConcept)
        } ?: ReasonConversionResult(ObjectConcept.We livesIn resourceConcept, resourceConcept))
    } ?: run {
        val rareResource = resourceTraction.entries.sortedBy { it.value }
                .randomElementOrNull { 1 - it.value.value.value }
                ?.key
                ?: return emptyReasonConversionResult()
        val resourceConcept = ArbitraryResource(rareResource)

        ReasonConversionResult(resourceConcept equals Rareness, resourceConcept) + rareResource.getAdditionalConcepts()
    }
}

fun Resource.getConcepts() = ReasonConversionResult(ArbitraryResource(this)) +
        getAdditionalConcepts()

fun List<Resource>.getResourceConcepts() = map { it.getConcepts() }
        .foldRight(emptyReasonConversionResult(), ReasonConversionResult::plus)

internal fun Resource.getAdditionalConcepts(): ReasonConversionResult {//TODO wind and death and all
    val takers = takers.filterIsInstance<Taker.ResourceTaker>()
            .map { ArbitraryResource(it.resource) }
    val takersReasonings = listOf(ArbitraryResource(this)) oppose takers

    return ReasonConversionResult(takersReasonings.toMutableList(), takers.toMutableList())
}

private fun takeOutRequest(turnRequests: RequestPool): ReasonConversionResult {
    var reasonResult = emptyReasonConversionResult()

    val (request, result) = turnRequests.resultStatus.entries
            .sortedBy { it.key.need }
            .randomElementOrNull { it.key.need.toDouble() }
            ?: return reasonResult
    for (type in request.types) {
        val resource = result.pack.resources.randomElementOrNull { it.amount.toDouble().pow(2) }

        if (0.5.testProbability()) {
            when (result.status) {
                ResultStatus.NotSatisfied -> {
                    reasonResult += makeNotSatisfiedRequestReasoning(type, resource)
                }
                ResultStatus.Satisfied -> {
                }
                ResultStatus.Excellent -> {
                }
            }
            continue
        }

        resource ?: continue

        val resourceConcept = ArbitraryResource(resource)
        reasonResult.concepts.add(resourceConcept)
        val concepts = when (type) {
            is RequestType.Food, is RequestType.Warmth, is RequestType.Clothes -> listOf<ReasonConcept>()
            is RequestType.Shelter -> listOf(Life)
            is RequestType.Vital -> listOf(Life, Good)
            is RequestType.Comfort -> listOf(Comfort, Good)
            is RequestType.Improvement -> listOf(Comfort, Good, Life, Change, Creation)
            is RequestType.Trade -> listOf(Change)
            is RequestType.Luxury -> listOf(Comfort)
            is RequestType.Spiritual -> listOf(Spirituality)
        }.toMutableList()
        concepts += listOf(type)

        reasonResult.reasonings.add(resourceConcept equals concepts.randomElement())
    }

    val additionalResourceReasonings = result.pack.resources.getResourceConcepts()

    return reasonResult + additionalResourceReasonings
}


private fun makeNotSatisfiedRequestReasoning(type: RequestType, resource: Resource?): ReasonConversionResult {
    0.5.chanceOf {
        return ReasonConversionResult(type equals listOf(Hardness, Hardship).randomElement(), type)
    }

    resource ?: return emptyReasonConversionResult()

    val resourceConcept = ArbitraryResource(resource)

    0.5.chanceOf {
        return ReasonConversionResult(
                resourceConcept equals listOf(Hardness, Hardship).randomElement(),
                resourceConcept
        )
    }

    return when (type) {
        RequestType.Food -> emptyReasonConversionResult()
        RequestType.Warmth -> emptyReasonConversionResult()
        RequestType.Clothes -> emptyReasonConversionResult()
        RequestType.Shelter -> emptyReasonConversionResult()
        RequestType.Vital -> emptyReasonConversionResult()
        RequestType.Comfort -> emptyReasonConversionResult()
        RequestType.Improvement -> emptyReasonConversionResult()
        RequestType.Trade -> emptyReasonConversionResult()
        RequestType.Luxury -> emptyReasonConversionResult()
        RequestType.Spiritual -> emptyReasonConversionResult()
    }
}
