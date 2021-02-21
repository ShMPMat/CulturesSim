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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractReasoning) return false

        if (meme != other.meme) return false

        return true
    }

    override fun hashCode(): Int {
        return meme.hashCode()
    }

    override fun toString() = meme.toString()
}


data class ReasonConclusion(val concept: ReasonConcept, val value: SoftValue)

fun ReasonConcept.toConclusion(value: Double) = ReasonConclusion(this, SoftValue(value))
fun ReasonConcept.toConclusion(value: SoftValue) = toConclusion(value.actualValue)


class ReasonComplex(val name: String, startReasonings: Set<Reasoning> = setOf()) {
    private val _reasonings = startReasonings.toMutableSet()
    val reasonings: Set<Reasoning> = _reasonings

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
        _reasonings.addAll(reasonings)
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
        startSpecialConcepts: Set<ReasonConcept> = setOf()
) {
    var commonReasonings = ReasonComplex(COMMON_REASONS)
        private set

    private val _reasoningComplexes = startReasonComplexes.toMutableList()
    val reasonComplexes: List<ReasonComplex> = _reasoningComplexes

    private val _specialConcepts = startSpecialConcepts.toMutableSet()
    val specialConcepts: Set<ReasonConcept> = _specialConcepts

    init {
        _reasoningComplexes.find { it.name == COMMON_REASONS }
                ?.let {
                    commonReasonings = it
                } ?: run {
            _reasoningComplexes.add(commonReasonings)
        }
    }

    fun addConcepts(concepts: List<ReasonConcept>) = _specialConcepts.addAll(concepts)

    fun fullCopy() = ReasonField(
            reasonComplexes.map { ReasonComplex(it.name, it.reasonings.toSet()) },
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

