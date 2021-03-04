package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonField
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept


interface ReasonConversion {
    fun makeConversion(complex: ReasonComplex): ReasonConversionResult

    fun enrichComplex(complex: ReasonComplex, field: ReasonField) = complex.apply {
        val (reasonings, concepts) = makeConversion(this)

        val added = addReasonings(reasonings)

        added.filterIsInstance<EqualityReasoning>()
                .forEach { field.manageIdeaConversion(it) }

        field.addConcepts(concepts)
    }
}
