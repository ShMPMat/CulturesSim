package shmp.generator.culture.worldview.reasoning

import shmp.random.singleton.randomElement
import shmp.generator.culture.worldview.reasoning.concept.IdeationalConcept
import shmp.generator.culture.worldview.reasoning.concept.ReasonConcept
import shmp.generator.culture.worldview.reasoning.convertion.IdeaConversion
import shmp.generator.culture.worldview.reasoning.convertion.ReasonConversion
import shmp.generator.culture.worldview.Meme


class ReasonField(
        val conversions: List<ReasonConversion>,
        reasonComplexes: List<ReasonComplex> = listOf(),
        specialConcepts: Set<ReasonConcept> = setOf(),
        specialConversions: List<ReasonConversion> = listOf()
) {
    var commonReasons = ReasonComplex(COMMON_REASONS)
        private set

    private val _reasonComplexes = reasonComplexes.toMutableList()
    val reasonComplexes: List<ReasonComplex> = _reasonComplexes

    private val _specialConcepts = specialConcepts.toMutableSet()
    val specialConcepts: Set<ReasonConcept> = _specialConcepts

    private val _specialConversions = specialConversions.toMutableList()
    val specialConversions: List<ReasonConversion> = _specialConversions

    init {
        val addedCommonReasons = _reasonComplexes.find { it.name == COMMON_REASONS }

        if (addedCommonReasons == null)
            _reasonComplexes.add(commonReasons)
        else
            commonReasons = addedCommonReasons
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

            val newEquivalents = appropriateConversions.flatMap { it.equivalentIdeas }
                    .toMutableSet()
            newEquivalents.add(equalityReasoning.objectConcept)
            newEquivalents.add(equalityReasoning.subjectConcept)

            _specialConversions.add(IdeaConversion(newEquivalents.toList()))
        }
    }

    fun update(reasonerMemes: List<Meme>): List<Reasoning> {
        val resultReasonings = mutableListOf<Reasoning>()

        for (complex in reasonComplexes) {
            val newReasonings = if (complex.isEmpty)
                listOf(generateBaseReasoning(reasonerMemes))
            else
                generateNewReasonings(this, complex)

            resultReasonings += complex.addReasonings(newReasonings)
        }

        val conversion = conversions.randomElement()
        resultReasonings += conversion.enrichComplex(commonReasons, this)

        return resultReasonings
    }

    fun copy(conversions: List<ReasonConversion> = this.conversions) = ReasonField(
            conversions,
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
