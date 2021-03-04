package shmp.simulation.culture.group.cultureaspect.reasoning.convertion

import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.cultureaspect.reasoning.EqualityReasoning
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.equals
import shmp.utils.without


data class IdeaConversion(val equivalentIdeas: List<IdeationalConcept>) : ReasonConversion {
    constructor(vararg equivalentIdeas: IdeationalConcept): this(equivalentIdeas.toList())

    override fun makeConversion(complex: ReasonComplex): ReasonConversionResult {
        val conversion = complex.reasonings
                .filterIsInstance<EqualityReasoning>()
                .filter { e -> e.any { it in equivalentIdeas } }
                .randomElementOrNull()
                ?: return emptyReasonConversionResult()

        return if (conversion.subjectConcept in equivalentIdeas) {
            val newIdea = equivalentIdeas.without(conversion.subjectConcept).randomElement()
            ReasonConversionResult(newIdea equals conversion.objectConcept)
        } else {
            val newIdea = equivalentIdeas.without(conversion.objectConcept).randomElement()
            ReasonConversionResult(conversion.subjectConcept equals newIdea)
        }
    }
}
