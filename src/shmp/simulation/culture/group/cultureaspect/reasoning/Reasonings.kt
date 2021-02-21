package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


open class BaseReasoning(
        override val meme: Meme,
        override val additionalMemes: List<Meme>,
        override val conclusions: List<ReasonConclusion>
) : AbstractReasoning()

class EqualityReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept) : BaseReasoning(
        MemeSubject("$objectConcept represents $subjectConcept"),
        listOf(objectConcept.meme, subjectConcept.meme),
        listOf()
)

class ConceptBoxReasoning(val concept: ReasonConcept) : AbstractReasoning() {
    override val meme = concept.meme
    override val additionalMemes = listOf<Meme>()
    override val conclusions = listOf<ReasonConclusion>()
}
