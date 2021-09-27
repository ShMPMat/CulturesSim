package shmp.simulation.culture.group.centers.util

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.AspectCore
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.TraitChange
import shmp.simulation.culture.group.centers.toChange
import shmp.simulation.culture.group.cultureaspect.CherishedResource
import shmp.simulation.culture.group.cultureaspect.Concept
import shmp.simulation.culture.group.cultureaspect.reasoning.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept.ArbitraryObjectConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.DeterminedConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept.*
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept.*
import shmp.simulation.culture.group.resource_behaviour.getRandom
import shmp.simulation.culture.group.stratum.Stratum
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.space.resource.Resource


class ArbitraryResource(val resource: Resource) : ArbitraryObjectConcept(Meme(resource.baseName))
class ArbitraryAspect(val aspect: Aspect) : ArbitraryActionConcept(Meme(aspect.name))
class ArbitraryStratum(val stratum: Stratum) : ArbitraryObjectConcept(Meme(stratum.baseName))


fun ReasonConclusion.toTraitChanges(): List<TraitChange> = when (concept) {
    is ArbitraryObjectConcept, World, AllLife,
    Self, Good, Bad, NoEvaluation, Uncertainty,
    Hardship, Comfort,
    Importance, Unimportance,
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
    is Change -> listOf(Trait.Discovery.toChange(value))
    is Permanence -> listOf(Trait.Discovery.toChange(-value))

    is DeterminedConcept -> concept.objectConcept.toConclusion(value).toTraitChanges() +
            concept.ideationalConcept.toConclusion(value).toTraitChanges()

    else -> throw GroupError("No trait conversion for a concept $this")
}

fun Reasoning.toConcept() = Concept(
        this.meme,
        this.conclusions.flatMap { it.toTraitChanges() }
)

fun Reasoning.toCherishedResource(): CherishedResource? {
    if (this !is ActionReasoning)
        return null

    return when (objectConcept) {
        is ArbitraryResource -> CherishedResource(this.objectConcept.resource, getRandom())
        else -> null
    }
}

