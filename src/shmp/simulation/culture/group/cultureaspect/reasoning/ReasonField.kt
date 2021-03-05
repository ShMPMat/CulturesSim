package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.IdeaConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion

class ReasonField(
        startReasonComplexes: List<ReasonComplex> = listOf(),
        startSpecialConcepts: Set<ReasonConcept> = setOf(),
        startSpecialConversions: Set<ReasonConversion> = setOf()
) {
    var commonReasonings = ReasonComplex(COMMON_REASONS)
        private set

    private val _reasoningComplexes = startReasonComplexes.toMutableList()
    val reasonComplexes: List<ReasonComplex> = _reasoningComplexes

    private val _specialConcepts = startSpecialConcepts.toMutableSet()
    val specialConcepts: Set<ReasonConcept> = _specialConcepts

    private val _specialConversions = startSpecialConversions.toMutableSet()
    val specialConversions: Set<ReasonConversion> = _specialConversions

    init {
        _reasoningComplexes.find { it.name == COMMON_REASONS }
                ?.let {
                    commonReasonings = it
                } ?: run {
            _reasoningComplexes.add(commonReasonings)
        }
    }

    fun addConcepts(concepts: List<ReasonConcept>) = _specialConcepts.addAll(concepts)

    fun manageIdeaConversion(equalityReasoning: EqualityReasoning) {
        if (equalityReasoning.isOppositions)
            return
        if (equalityReasoning.subjectConcept !is IdeationalConcept)
            return
        if (equalityReasoning.objectConcept !is IdeationalConcept)
            return

        val appropriateConversions = _specialConversions.filterIsInstance<IdeaConversion>()
                .filter { c -> equalityReasoning.any { it in c.equivalentIdeas } }

        if (appropriateConversions.isEmpty()) {
            _specialConversions.add(IdeaConversion(equalityReasoning.subjectConcept, equalityReasoning.objectConcept))
        } else {
            _specialConversions.removeAll(appropriateConversions)

            val newEquivalents = appropriateConversions.flatMap { it.equivalentIdeas }.toMutableSet()
            newEquivalents.add(equalityReasoning.objectConcept)
            newEquivalents.add(equalityReasoning.subjectConcept)

            _specialConversions.add(IdeaConversion(newEquivalents.toList()))
        }
    }

    fun copy() = ReasonField(
            reasonComplexes.map { ReasonComplex(it.name, it.reasonings.toSet()) },
            specialConcepts,
            specialConversions
    )

    override fun toString() = """
        |Complexes:
        |${reasonComplexes.joinToString("\n")}
        |
        |Special Concepts:
        |${specialConcepts.joinToString("\n")}
        |
        |Special Conversions:
        |${specialConversions.joinToString("\n")}
    """.trimMargin()
}

const val COMMON_REASONS = "Common reasons"