package io.tashtabash.sim.culture.group.centers.util

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.*
import io.tashtabash.sim.culture.group.centers.MemoryCenter
import io.tashtabash.sim.culture.group.centers.util.ReasoningRandom.*
import io.tashtabash.generator.culture.worldview.reasoning.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversion
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversionResult
import io.tashtabash.generator.culture.worldview.reasoning.convertion.emptyReasonConversionResult
import io.tashtabash.sim.culture.group.request.RequestPool
import io.tashtabash.sim.culture.group.request.RequestType
import io.tashtabash.sim.culture.group.request.ResultStatus
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.utils.MovingAverage
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
        when (entries.randomElement()) {
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
