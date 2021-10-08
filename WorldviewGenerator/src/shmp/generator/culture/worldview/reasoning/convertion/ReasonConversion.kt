package shmp.generator.culture.worldview.reasoning.convertion

import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.ReasonField
import shmp.generator.culture.worldview.reasoning.Reasoning


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
