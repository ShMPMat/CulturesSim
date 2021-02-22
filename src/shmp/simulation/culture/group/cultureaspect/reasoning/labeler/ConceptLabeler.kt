package shmp.simulation.culture.group.cultureaspect.reasoning.labeler

import shmp.simulation.culture.group.cultureaspect.concept.ReasonConcept


interface ConceptLabeler {
    fun matches(concept: ReasonConcept): Boolean

    fun takeIfMatches(concept: ReasonConcept) = concept.takeIf { matches(it) }
}

data class EqualityConceptLabeler(val concept: ReasonConcept) : ConceptLabeler {
    override fun matches(sample: ReasonConcept) = sample == concept
}
