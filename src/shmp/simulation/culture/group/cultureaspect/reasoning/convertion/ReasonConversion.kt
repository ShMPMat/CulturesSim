package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonField
import shmp.simulation.culture.group.cultureaspect.reasoning.Reasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept


interface ReasonConversion {
    fun makeConversion(complex: ReasonComplex): ReasonConversionResult

    fun enrichComplex(complex: ReasonComplex, field: ReasonField): List<Reasoning> {
        val (reasonings, concepts) = makeConversion(complex)

        val added = complex.addReasonings(reasonings)

        added.filterIsInstance<EqualityReasoning>()
                .forEach { field.manageIdeaConversion(it) }

        field.addConcepts(concepts)

        return added
    }
}
