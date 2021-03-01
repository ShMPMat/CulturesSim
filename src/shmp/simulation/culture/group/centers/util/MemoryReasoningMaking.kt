package shmp.simulation.culture.group.centers.util

import shmp.random.SampleSpaceObject
import shmp.random.randomElementOrNull
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.equals
import shmp.simulation.culture.group.request.RequestPool
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.Result
import shmp.simulation.culture.group.request.ResultStatus
import shmp.simulation.space.resource.Resource
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
        when (ReasoningRandom.values().randomElement()) {
            ReasoningRandom.Requests -> takeOutRequest(memoryCenter.turnRequests)
            ReasoningRandom.MemoryResources -> takeOutResourceTraction(memoryCenter.resourceTraction)
        }

private fun takeOutResourceTraction(resourceTraction: Map<Resource, MovingAverage>): ReasonConversionResult {
    return if (0.5.testProbability()) {
        val commonResource = randomElementOrNull(resourceTraction.entries.sortedBy { it.value }, { it.value.value.value }, Controller.session.random)
                ?.key
                ?: return emptyReasonConversionResult()
        val resourceConcept = ArbitraryResource(commonResource)

        ReasonConversionResult(resourceConcept equals IdeationalConcept.Commonness, resourceConcept)
    } else {
        val rareResource = randomElementOrNull(resourceTraction.entries.sortedBy { it.value }, { 1 - it.value.value.value }, Controller.session.random)
                ?.key
                ?: return emptyReasonConversionResult()
        val resourceConcept = ArbitraryResource(rareResource)

        ReasonConversionResult(resourceConcept equals IdeationalConcept.Rareness, resourceConcept)
    }
}

private fun takeOutRequest(turnRequests: RequestPool): ReasonConversionResult {
    val reasonResult = emptyReasonConversionResult()

    val (request, result) = turnRequests.resultStatus.entries
            .sortedBy { it.key.need }
            .randomElementOrNull { it.key.need.toDouble() }
            ?: return reasonResult
    for (type in request.types) {
        if (result.status == ResultStatus.NotSatisfied && 0.5.testProbability()) {
            reasonResult += makeNotSatisfiedRequestReasoning(type, result)
            continue
        }

        val resource = result.pack.resources.randomElementOrNull { it.amount.toDouble().pow(2) }
                ?: continue
        val resourceConcept = ArbitraryResource(resource)
        reasonResult.concepts.add(resourceConcept)
        when(type) {
            is RequestType.Food -> {}
            is RequestType.Warmth -> {}
            is RequestType.Clothes -> {}
            is RequestType.Shelter -> reasonResult.reasonings.add(
                    listOf(
                            resourceConcept equals IdeationalConcept.Life
                    ).randomElement()
            )
            is RequestType.Vital -> reasonResult.reasonings.add(
                    listOf(
                            resourceConcept equals IdeationalConcept.Life,
                            resourceConcept equals IdeationalConcept.Good
                    ).randomElement()
            )
            is RequestType.Comfort -> reasonResult.reasonings.add(
                    listOf(
                            resourceConcept equals IdeationalConcept.Comfort,
                            resourceConcept equals IdeationalConcept.Good
                    ).randomElement()
            )
            is RequestType.Improvement -> reasonResult.reasonings.add(
                    listOf(
                            resourceConcept equals IdeationalConcept.Comfort,
                            resourceConcept equals IdeationalConcept.Good,
                            resourceConcept equals IdeationalConcept.Life,
                            resourceConcept equals IdeationalConcept.Change,
                            resourceConcept equals IdeationalConcept.Creation,
                    ).randomElement()
            )
            is RequestType.Trade -> reasonResult.reasonings.add(
                    listOf(
                            resourceConcept equals IdeationalConcept.Change
                    ).randomElement()
            )
            is RequestType.Luxury -> reasonResult.reasonings.add(
                    listOf(
                            resourceConcept equals IdeationalConcept.Comfort
                    ).randomElement()
            )
        }
    }

    return reasonResult
}

private fun makeNotSatisfiedRequestReasoning(type: RequestType, result: Result): ReasonConversionResult {
    if (0.5.testProbability())
        return ReasonConversionResult(type equals IdeationalConcept.Hardness, type)

    val resource = result.pack.resources.randomElementOrNull { it.amount.toDouble().pow(2) }
            ?: return emptyReasonConversionResult()
    val resourceConcept = ArbitraryResource(resource)

    if (0.5.testProbability())
        return ReasonConversionResult(resourceConcept equals IdeationalConcept.Hardness, resourceConcept)

    return when(type) {
        RequestType.Food -> emptyReasonConversionResult()
        RequestType.Warmth -> emptyReasonConversionResult()
        RequestType.Clothes -> emptyReasonConversionResult()
        RequestType.Shelter -> emptyReasonConversionResult()
        RequestType.Vital -> emptyReasonConversionResult()
        RequestType.Comfort -> emptyReasonConversionResult()
        RequestType.Improvement -> emptyReasonConversionResult()
        RequestType.Trade -> emptyReasonConversionResult()
        RequestType.Luxury -> emptyReasonConversionResult()
    }
}
