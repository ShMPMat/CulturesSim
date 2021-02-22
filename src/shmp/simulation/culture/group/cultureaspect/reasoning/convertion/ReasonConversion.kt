package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.Reasoning


interface ReasonConversion {
    fun makeConversion(reasonComplex: ReasonComplex): List<Reasoning>
}
