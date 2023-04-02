package io.tashtabash.generator.culture.worldview.reasoning.convertion

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.generator.culture.worldview.reasoning.EqualityReasoning
import io.tashtabash.generator.culture.worldview.reasoning.ReasonComplex
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept
import io.tashtabash.generator.culture.worldview.reasoning.equals
import io.tashtabash.utils.without


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
