package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.utils.SoftValue


interface Reasoning {
    val meme: Meme
    val additionalMemes: List<Meme>

    val conclusions: List<ReasonConclusion>
}

abstract class AbstractReasoning : Reasoning {
    override fun toString() = meme.toString()
}


data class ReasonConclusion(val concept: ReasonConcept, val value: SoftValue)

fun ReasonConcept.toConclusion(value: Double) = ReasonConclusion(this, SoftValue(value))
fun ReasonConcept.toConclusion(value: SoftValue) = toConclusion(value.actualValue)


class ReasonComplex(val name: String, startReasonings: List<Reasoning> = listOf()) {
    private val internalReasonings = startReasonings.toMutableList()
    val reasonings: List<Reasoning> = internalReasonings

    val condensedConclusions: List<ReasonConclusion>
        get() = reasonings
                .flatMap { it.conclusions }
                .groupBy { it.concept }
                .map { (concept, conclusions) ->
                    val sum = conclusions
                            .map { it.value }
                            .foldRight(SoftValue(), SoftValue::plus)
                    ReasonConclusion(concept, sum)
                }

    val isEmpty: Boolean
        get() = reasonings.isEmpty()

    fun addReasonings(reasonings: List<Reasoning>) = internalReasonings.addAll(reasonings)

    override fun toString() = """
        |$name
        |${reasonings.joinToString("\n")}
        """
}

const val COMMON_REASONS = "Common reasons"

class ReasonField(
        startReasonComplexes: List<ReasonComplex> = listOf(ReasonComplex(COMMON_REASONS)),
        startSpecialConcepts: List<ReasonConcept> = listOf()
) {
    private val internalReasoningComplexes = startReasonComplexes.toMutableList()
    val reasonComplexes: List<ReasonComplex> = internalReasoningComplexes

    private val internalSpecialConcepts = startSpecialConcepts.toMutableList()
    val specialConcepts: List<ReasonConcept> = internalSpecialConcepts

    fun fullCopy() = ReasonField(
            reasonComplexes.map { ReasonComplex(it.name, it.reasonings.toList()) },
            specialConcepts
    )

    override fun toString() = """
        |Complexes:
        |${reasonComplexes.joinToString("\n")}
        |
        |Special Concepts:
        |${specialConcepts.joinToString("\n")}
    """.trimMargin()
}


class BaseReasoning(
        override val meme: Meme,
        override val additionalMemes: List<Meme>,
        override val conclusions: List<ReasonConclusion>
) : AbstractReasoning()

class ConceptBoxReasoning(val concept: ReasonConcept) : AbstractReasoning() {
    override val meme = concept.meme
    override val additionalMemes = listOf<Meme>()
    override val conclusions = listOf<ReasonConclusion>()
}

