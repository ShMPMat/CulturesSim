package shmp.simulation.culture.group.centers.util

import shmp.simulation.Controller.session
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.TraitChange
import shmp.simulation.culture.group.centers.toChange
import shmp.simulation.culture.group.cultureaspect.Concept
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonConclusion
import shmp.simulation.culture.group.cultureaspect.reasoning.Reasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept.ArbitraryObjectConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.DeterminedConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.toConclusion
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.space.resource.Resource
import kotlin.random.Random


class ArbitraryResource(objectMeme: Meme, val resource: Resource) : ArbitraryObjectConcept(objectMeme)


fun ReasonConclusion.toTraitChanges(): List<TraitChange> = when (concept) {
    is ArbitraryObjectConcept, World, AllLife,
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

class MemoryConversion(private val memoryCenter: MemoryCenter) : ReasonConversion {
    override fun makeConversion(complex: ReasonComplex, random: Random) =
            takeOutCommonReasonings(memoryCenter, session.random)
}
