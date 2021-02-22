package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.concept.ReasonConcept

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

const val COMMON_REASONS = "Common reasons"
