package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.concept.ReasonConcept


infix fun ReasonConcept.equals(other: ReasonConcept) = EqualityReasoning(this, other)
