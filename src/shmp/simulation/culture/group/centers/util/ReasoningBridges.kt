package shmp.simulation.culture.group.centers.util

import shmp.random.randomElement
import shmp.random.randomElementOrNull
import shmp.random.testProbability
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.TraitChange
import shmp.simulation.culture.group.centers.toChange
import shmp.simulation.culture.group.cultureaspect.Concept
import shmp.simulation.culture.group.cultureaspect.concept.DeterminedConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.*
import shmp.simulation.culture.group.cultureaspect.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.concept.ObjectConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonAdditionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonAdditionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.singletonReasonAdditionResult
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.Result
import shmp.simulation.culture.group.request.ResultStatus
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.space.resource.Resource
import kotlin.math.pow
import kotlin.random.Random


class ArbitraryResource(objectMeme: Meme, val resource: Resource) : ArbitraryObject(objectMeme)


fun ReasonConclusion.toTraitChanges(): List<TraitChange> = when (concept) {
    is ArbitraryObject, World, AllLife,
    Self, Good, Bad, NoEvaluation, Uncertainty,
    Hardship, Comfort,
    Importance, Unimportance,
    Change, Permanence,
    Life, Death,
    Uniqueness, Commonness,
    Simpleness, Complexity -> listOf()

    is Peace -> listOf(Trait.Peace.toChange(value))
    is War -> listOf(Trait.Peace.toChange(-value))
    is Expansion -> listOf(Trait.Expansion.toChange(value))
    is Content -> listOf(Trait.Expansion.toChange(-value))
    is Consolidation -> listOf(Trait.Consolidation.toChange(value))
    is Freedom -> listOf(Trait.Consolidation.toChange(-value))
    is Creation -> listOf(Trait.Creation.toChange(value))
    is Destruction -> listOf(Trait.Creation.toChange(-value))

    is DeterminedConcept -> concept.objectConcept.toConclusion(value).toTraitChanges() +
            concept.ideationalConcept.toConclusion(value).toTraitChanges()

    else -> throw GroupError("No trait conversion for a concept $this")
}

fun Reasoning.toConcept() = Concept(
        this.meme,
        this.conclusions.flatMap { it.toTraitChanges() }
)

fun takeOutCommonReasonings(memoryCenter: MemoryCenter, random: Random): ReasonAdditionResult {
    val reasonResult = emptyReasonAdditionResult()

    val (request, result) = randomElementOrNull(
            memoryCenter.turnRequests.resultStatus.entries.sortedBy { it.key.need },
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
                            resourceConcept equals Life
                    ),
                    random
            ))
            is RequestType.Vital -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals Life,
                            resourceConcept equals Good
                    ),
                    random
            ))
            is RequestType.Comfort -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals Comfort,
                            resourceConcept equals Good
                    ),
                    random
            ))
            is RequestType.Improvement -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals Comfort,
                            resourceConcept equals Good,
                            resourceConcept equals Life,
                            resourceConcept equals Change,
                            resourceConcept equals Creation,
                    ),
                    random
            ))
            is RequestType.Trade -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals Change
                    ),
                    random
            ))
            is RequestType.Luxury -> reasonResult.reasonings.add(randomElement(
                    listOf(
                            resourceConcept equals Comfort
                    ),
                    random
            ))
        }
    }

    return reasonResult
}

private fun makeNotSatisfiedRequestReasoning(type: RequestType, result: Result, random: Random): ReasonAdditionResult {
    if (testProbability(0.5, random))
        return singletonReasonAdditionResult(type equals Hardness, type)

    val resource = randomElementOrNull(result.pack.resources, { it.amount.toDouble().pow(2) }, random)
            ?: return emptyReasonAdditionResult()
    val resourceConcept = ArbitraryResource(MemeSubject(resource.baseName), resource)

    if (testProbability(0.5, random))
        return singletonReasonAdditionResult(resourceConcept equals Hardness, resourceConcept)

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
