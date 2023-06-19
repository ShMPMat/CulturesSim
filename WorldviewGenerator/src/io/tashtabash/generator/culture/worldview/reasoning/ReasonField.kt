package io.tashtabash.generator.culture.worldview.reasoning

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.reasoning.convertion.IdeaConversion
import io.tashtabash.generator.culture.worldview.reasoning.convertion.ReasonConversion
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept


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

    fun copy(reasonerMemeSubstitution: Map<Meme, Meme>, conversions: List<ReasonConversion> = this.conversions): ReasonField {
        val reasonerSubstitution: Map<ReasonConcept, ReasonConcept> = reasonerMemeSubstitution.entries.associate { (old, new) ->
            ObjectConcept.ArbitraryObjectConcept(old) to ObjectConcept.ArbitraryObjectConcept(new)
        }
        val newReasonComplexes = reasonComplexes.map { c ->
            ReasonComplex(
                    c.name,
                    c.reasonings
                            .mapNotNull { it.substitute(reasonerSubstitution) }
                            .toSet()
            )
        }

        return ReasonField(conversions, newReasonComplexes, specialConcepts, specialConversions)
    }

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
