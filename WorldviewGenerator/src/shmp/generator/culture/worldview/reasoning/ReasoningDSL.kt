package shmp.generator.culture.worldview.reasoning

import shmp.generator.culture.worldview.reasoning.concept.ActionConcept
import shmp.generator.culture.worldview.reasoning.concept.ObjectConcept
import shmp.generator.culture.worldview.reasoning.concept.ReasonConcept


infix fun ReasonConcept.equals(other: ReasonConcept) =
        EqualityReasoning(this, other)
infix fun ReasonConcept.equalsAll(other: Collection<ReasonConcept>) =
        listOf(this) equal other
infix fun Collection<ReasonConcept>.equal(others: Collection<ReasonConcept>) =
        flatMap { t -> others.map { o -> EqualityReasoning(t, o) } }

infix fun ReasonConcept.associatesWith(other: ReasonConcept) =
        AssociationReasoning(this, other)
infix fun Collection<ReasonConcept>.associateWith(others: Collection<ReasonConcept>) =
        flatMap { t -> others.map { o -> AssociationReasoning(t, o) } }

infix fun ReasonConcept.opposes(other: ReasonConcept) =
        OppositionReasoning(this, other)
infix fun ReasonConcept.oppose(other: Collection<ReasonConcept>) =
        listOf(this) oppose other
infix fun Collection<ReasonConcept>.oppose(other: Collection<ReasonConcept>) =
        flatMap { t -> other.map { o -> OppositionReasoning(t, o) } }
infix fun Collection<ReasonConcept>.oppose(other: ReasonConcept) =
        map { t -> OppositionReasoning(t, other) }

infix fun ReasonConcept.needs(other: ActionConcept) =
        ActionReasoning(this, other)

infix fun ObjectConcept.livesIn(other: ReasonConcept) =
        ExistenceInReasoning(this, other)
