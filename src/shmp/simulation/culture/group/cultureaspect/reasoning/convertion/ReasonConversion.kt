package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonField


interface ReasonConversion {
    fun makeConversion(complex: ReasonComplex): ReasonConversionResult

    fun enrichComplex(complex: ReasonComplex, field: ReasonField) = complex.apply {
        val (reasonings, concepts) = makeConversion(this)

        addReasonings(reasonings)
        field.addConcepts(concepts)
    }
}
