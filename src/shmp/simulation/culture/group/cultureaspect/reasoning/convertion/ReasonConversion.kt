package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonField
import kotlin.random.Random


interface ReasonConversion {
    fun makeConversion(complex: ReasonComplex, random: Random): ReasonConversionResult

    fun enrichComplex(complex: ReasonComplex, field: ReasonField, random: Random) = complex.apply {
        val (reasonings, concepts) = makeConversion(this, random)

        addReasonings(reasonings)
        field.addConcepts(concepts)
    }
}
