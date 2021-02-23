package shmp.simulation.culture.group.centers.util

import shmp.random.randomElement
import shmp.random.randomElementOrNull
import shmp.random.testProbability
import shmp.simulation.Controller.session
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
import shmp.simulation.culture.group.request.*
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.space.resource.Resource
import shmp.utils.MovingAverage
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
