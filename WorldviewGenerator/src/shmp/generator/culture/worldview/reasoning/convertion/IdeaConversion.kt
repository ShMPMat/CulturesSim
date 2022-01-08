package shmp.generator.culture.worldview.reasoning.convertion

import shmp.random.singleton.randomElement
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept
import shmp.generator.culture.worldview.reasoning.equals
import shmp.utils.without


data class IdeaConversion(val equivalentIdeas: List<IdeationalConcept>) : ReasonConversion {
    constructor(vararg equivalentIdeas: IdeationalConcept): this(equivalentIdeas.toList())

    override fun makeConversion(complex: ReasonComplex) = complex.calculateOn<EqualityReasoning> {
        filter { e -> e.any { it in equivalentIdeas } }

        withRandom { r ->
            if (r.subjectConcept in equivalentIdeas) {
                val newIdea = equivalentIdeas.without(r.subjectConcept).randomElement()
                newIdea equals r.objectConcept
            } else {
                val newIdea = equivalentIdeas.without(r.objectConcept).randomElement()
                r.subjectConcept equals newIdea
            }.toConversionResult()
        }
    }
}
