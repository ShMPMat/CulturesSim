package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept


infix fun ReasonConcept.equals(other: ReasonConcept) = EqualityReasoning(this, other)
infix fun Collection<ReasonConcept>.equal(other: Collection<ReasonConcept>) =
        this.flatMap { t -> other.map { o -> EqualityReasoning(t, o) } }

infix fun ReasonConcept.opposes(other: ReasonConcept) = OppositionReasoning(this, other)
infix fun Collection<ReasonConcept>.oppose(other: Collection<ReasonConcept>) =
        this.flatMap { t -> other.map { o -> OppositionReasoning(t, o) } }

infix fun ReasonConcept.needs(other: ActionConcept) = NeedReasoning(this, other)
