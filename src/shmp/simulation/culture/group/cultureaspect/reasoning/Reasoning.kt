package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject
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

    /***
     * returns: List of accepted Reasonings
     */
    fun addReasonings(reasonings: List<Reasoning>): List<Reasoning> {
        internalReasonings.addAll(reasonings)
        return reasonings
    }

    override fun toString() = """
        |$name
        |${reasonings.joinToString("\n")}
        """
}

const val COMMON_REASONS = "Common reasons"

class ReasonField(
        startReasonComplexes: List<ReasonComplex> = listOf(),
        startSpecialConcepts: List<ReasonConcept> = listOf()
) {
    val commonReasonings = ReasonComplex(COMMON_REASONS)

    private val internalReasoningComplexes = startReasonComplexes.toMutableList() + listOf(commonReasonings)
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


open class BaseReasoning(
        override val meme: Meme,
        override val additionalMemes: List<Meme>,
        override val conclusions: List<ReasonConclusion>
) : AbstractReasoning()

class EqualityReasoning(val objectConcept: ReasonConcept, val subjectConcept: ReasonConcept): BaseReasoning(
        MemeSubject("$objectConcept represents $subjectConcept"),
        listOf(objectConcept.meme, subjectConcept.meme),
        listOf()
)

class ConceptBoxReasoning(val concept: ReasonConcept) : AbstractReasoning() {
    override val meme = concept.meme
    override val additionalMemes = listOf<Meme>()
    override val conclusions = listOf<ReasonConclusion>()
}

