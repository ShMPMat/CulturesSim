package shmp.simulation.culture.group.centers.util

import shmp.simulation.culture.aspect.AspectCore
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.TraitChange
import shmp.simulation.culture.group.centers.toChange
import shmp.simulation.culture.group.cultureaspect.Concept
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonConclusion
import shmp.simulation.culture.group.cultureaspect.reasoning.Reasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept.ArbitraryObjectConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.DeterminedConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.toConclusion
import shmp.simulation.culture.group.stratum.Stratum
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.space.resource.Resource


class ArbitraryResource(val resource: Resource) : ArbitraryObjectConcept(MemeSubject(resource.baseName))
class ArbitraryAspect(val aspectCore: AspectCore) : ArbitraryActionConcept(MemeSubject(aspectCore.name))
class ArbitraryStratum(val stratum: Stratum) : ArbitraryObjectConcept(MemeSubject(stratum.baseName))


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
