package shmp.simulation.culture.group.centers.util

import shmp.random.randomElement
import shmp.random.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.random.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonAdditionResult
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
import kotlin.random.Random


fun takeOutCommonReasonings(memoryCenter: MemoryCenter, random: Random): ReasonConversionResult {
    return if (0.5.testProbability()) {
        takeOutRequest(memoryCenter.turnRequests, random)
    } else {
        takeOutResourceTraction(memoryCenter.resourceTraction, Controller.session.random)
    }
}

fun takeOutResourceTraction(resourceTraction: Map<Resource, MovingAverage>, random: Random?): ReasonConversionResult {
    return if (0.5.testProbability()) {
        val commonResource = randomElementOrNull(resourceTraction.entries.sortedBy { it.value }, { it.value.value.value }, Controller.session.random)
                ?.key
                ?: return emptyReasonAdditionResult()
        val resourceConcept = ArbitraryResource(MemeSubject(commonResource.baseName), commonResource)

        singletonReasonAdditionResult(resourceConcept equals IdeationalConcept.Commonness, resourceConcept)
    } else {
        val rareResource = randomElementOrNull(resourceTraction.entries.sortedBy { it.value }, { 1 - it.value.value.value }, Controller.session.random)
                ?.key
                ?: return emptyReasonAdditionResult()
        val resourceConcept = ArbitraryResource(MemeSubject(rareResource.baseName), rareResource)

        singletonReasonAdditionResult(resourceConcept equals IdeationalConcept.Rareness, resourceConcept)
    }
}

fun takeOutRequest(turnRequests: RequestPool, random: Random): ReasonConversionResult {
    val reasonResult = emptyReasonAdditionResult()

    val (request, result) = randomElementOrNull(
            turnRequests.resultStatus.entries.sortedBy { it.key.need },
            { it.key.need.toDouble() },
            random
    ) ?: return reasonResult
    for (type in request.types) {
        if (result.status == ResultStatus.NotSatisfied && testProbability(0.5, random)) {
            reasonResult += makeNotSatisfiedRequestReasoning(type, result, random)
            continue
        }

        val resource = randomElementOrNull(result.pack.resources, { it.amount.toDouble().pow(2) }, random)
                ?: continue
        val resourceConcept = ArbitraryResource(MemeSubject(resource.baseName), resource)
        reasonResult.concepts.add(resourceConcept)
        when(type) {
            is RequestType.Food -> {}
            is RequestType.Warmth -> {}
            is RequestType.Clothes -> {}
            is RequestType.Shelter -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals IdeationalConcept.Life
                    ),
                    random
            ))
            is RequestType.Vital -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals IdeationalConcept.Life,
                            resourceConcept equals IdeationalConcept.Good
                    ),
                    random
            ))
            is RequestType.Comfort -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals IdeationalConcept.Comfort,
                            resourceConcept equals IdeationalConcept.Good
                    ),
                    random
            ))
            is RequestType.Improvement -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals IdeationalConcept.Comfort,
                            resourceConcept equals IdeationalConcept.Good,
                            resourceConcept equals IdeationalConcept.Life,
                            resourceConcept equals IdeationalConcept.Change,
                            resourceConcept equals IdeationalConcept.Creation,
                    ),
                    random
            ))
            is RequestType.Trade -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals IdeationalConcept.Change
                    ),
                    random
            ))
            is RequestType.Luxury -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals IdeationalConcept.Comfort
                    ),
                    random
            ))
        }
    }

    return reasonResult
}

private fun makeNotSatisfiedRequestReasoning(type: RequestType, result: Result, random: Random): ReasonConversionResult {
    if (testProbability(0.5, random))
        return singletonReasonAdditionResult(type equals IdeationalConcept.Hardness, type)

    val resource = randomElementOrNull(result.pack.resources, { it.amount.toDouble().pow(2) }, random)
            ?: return emptyReasonAdditionResult()
    val resourceConcept = ArbitraryResource(MemeSubject(resource.baseName), resource)

    if (testProbability(0.5, random))
        return singletonReasonAdditionResult(resourceConcept equals IdeationalConcept.Hardness, resourceConcept)

    return when(type) {
        RequestType.Food -> emptyReasonAdditionResult()
        RequestType.Warmth -> emptyReasonAdditionResult()
        RequestType.Clothes -> emptyReasonAdditionResult()
        RequestType.Shelter -> emptyReasonAdditionResult()
        RequestType.Vital -> emptyReasonAdditionResult()
        RequestType.Comfort -> emptyReasonAdditionResult()
        RequestType.Improvement -> emptyReasonAdditionResult()
        RequestType.Trade -> emptyReasonAdditionResult()
        RequestType.Luxury -> emptyReasonAdditionResult()
    }
}
