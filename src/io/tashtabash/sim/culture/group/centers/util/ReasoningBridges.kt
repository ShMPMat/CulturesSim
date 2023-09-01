package io.tashtabash.sim.culture.group.centers.util

import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.group.GroupError
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.TraitChange
import io.tashtabash.sim.culture.group.centers.toChange
import io.tashtabash.sim.culture.group.cultureaspect.CherishedResource
import io.tashtabash.sim.culture.group.cultureaspect.Concept
import io.tashtabash.generator.culture.worldview.reasoning.ActionReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonConclusion
import io.tashtabash.generator.culture.worldview.reasoning.Reasoning
import io.tashtabash.generator.culture.worldview.reasoning.concept.ActionConcept.ArbitraryActionConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.DeterminedConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.toConclusion
import io.tashtabash.sim.culture.group.request.RequestType
import io.tashtabash.sim.culture.group.resource_behaviour.getRandom
import io.tashtabash.sim.culture.group.stratum.Stratum
import io.tashtabash.sim.space.resource.Resource


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

    is DeterminedConcept -> (concept as DeterminedConcept).let {
        it.objectConcept.toConclusion(value).toTraitChanges() +
                it.ideationalConcept.toConclusion(value).toTraitChanges()
    }

    // It's here in case I'd like to add some instances before it
    is RequestType -> listOf()

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
        is ArbitraryResource -> CherishedResource((objectConcept as ArbitraryResource).resource, getRandom())
        else -> null
    }
}

