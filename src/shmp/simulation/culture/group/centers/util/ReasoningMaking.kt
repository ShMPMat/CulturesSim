package shmp.simulation.culture.group.centers.util

import shmp.random.randomElementOrNull
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.singletonReasonAdditionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.equals
import shmp.simulation.culture.group.request.RequestPool
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.Result
import shmp.simulation.culture.group.request.ResultStatus
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.space.resource.Resource
import shmp.utils.MovingAverage
import kotlin.math.pow


fun takeOutCommonReasonings(memoryCenter: MemoryCenter): ReasonConversionResult {
    return if (0.5.testProbability())
        takeOutRequest(memoryCenter.turnRequests)
    else
        takeOutResourceTraction(memoryCenter.resourceTraction)
}

fun takeOutResourceTraction(resourceTraction: Map<Resource, MovingAverage>): ReasonConversionResult {
    return if (0.5.testProbability()) {
        val commonResource = randomElementOrNull(resourceTraction.entries.sortedBy { it.value }, { it.value.value.value }, Controller.session.random)
                ?.key
                ?: return emptyReasonConversionResult()
        val resourceConcept = ArbitraryResource(MemeSubject(commonResource.baseName), commonResource)

        singletonReasonAdditionResult(resourceConcept equals IdeationalConcept.Commonness, resourceConcept)
    } else {
        val rareResource = randomElementOrNull(resourceTraction.entries.sortedBy { it.value }, { 1 - it.value.value.value }, Controller.session.random)
                ?.key
                ?: return emptyReasonConversionResult()
        val resourceConcept = ArbitraryResource(MemeSubject(rareResource.baseName), rareResource)

        singletonReasonAdditionResult(resourceConcept equals IdeationalConcept.Rareness, resourceConcept)
    }
}

fun takeOutRequest(turnRequests: RequestPool): ReasonConversionResult {
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
        val resourceConcept = ArbitraryResource(MemeSubject(resource.baseName), resource)
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
        return singletonReasonAdditionResult(type equals IdeationalConcept.Hardness, type)

    val resource = result.pack.resources.randomElementOrNull { it.amount.toDouble().pow(2) }
            ?: return emptyReasonConversionResult()
    val resourceConcept = ArbitraryResource(MemeSubject(resource.baseName), resource)

    if (0.5.testProbability())
        return singletonReasonAdditionResult(resourceConcept equals IdeationalConcept.Hardness, resourceConcept)

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
