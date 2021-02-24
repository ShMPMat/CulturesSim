package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ActionConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept


infix fun ReasonConcept.equals(other: ReasonConcept) = EqualityReasoning(this, other)
infix fun ReasonConcept.opposes(other: ReasonConcept) = OppositionReasoning(this, other)
infix fun ReasonConcept.needs(other: ActionConcept) = NeedReasoning(this, other)
