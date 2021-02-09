package shmp.simulation.culture.group.centers.util

import shmp.random.randomElement
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.centers.TraitChange
import shmp.simulation.culture.group.centers.toChange
import shmp.simulation.culture.group.cultureaspect.Concept
import shmp.simulation.culture.group.cultureaspect.reasoning.*
import shmp.simulation.culture.group.cultureaspect.reasoning.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.ObjectConcept.*
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.space.resource.Resource
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

    is Peace -> listOf(shmp.simulation.culture.group.centers.Trait.Peace.toChange(value))
    is War -> listOf(shmp.simulation.culture.group.centers.Trait.Peace.toChange(-value))
    is Expansion -> listOf(shmp.simulation.culture.group.centers.Trait.Expansion.toChange(value))
    is Content -> listOf(shmp.simulation.culture.group.centers.Trait.Expansion.toChange(-value))
    is Consolidation -> listOf(shmp.simulation.culture.group.centers.Trait.Consolidation.toChange(value))
    is Freedom -> listOf(shmp.simulation.culture.group.centers.Trait.Consolidation.toChange(-value))
    is Creation -> listOf(shmp.simulation.culture.group.centers.Trait.Creation.toChange(value))
    is Destruction -> listOf(shmp.simulation.culture.group.centers.Trait.Creation.toChange(-value))

    is DeterminedConcept -> concept.objectConcept.toConclusion(value).toTraitChanges() +
            concept.ideationalConcept.toConclusion(value).toTraitChanges()

    else -> throw GroupError("No trait conversion for a concept $this")
}

fun Reasoning.toConcept() = Concept(
        this.meme,
        this.conclusions.flatMap { it.toTraitChanges() }
)

fun takeOutCommonReasonings(memoryCenter: MemoryCenter, random: Random): List<Reasoning> {
    val (request, result) = randomElement(
            memoryCenter.turnRequests.resultStatus.entries.sortedBy { it.key.need },
            { it.key.need.toDouble() },
            random
    )
//    request.evaluator.labeler.

    return emptyList()
}
