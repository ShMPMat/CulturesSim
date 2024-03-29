package io.tashtabash.generator.culture.worldview.reasoning

import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.utils.SoftValue


class ReasonComplex(val name: String, reasonings: Set<Reasoning> = setOf()) {
    private val _reasonings = reasonings.toMutableSet()
    val reasonings: Set<Reasoning> = _reasonings

    val equalityReasonings: List<EqualityReasoning>
        get() = _reasonings.filterIsInstance<EqualityReasoning>()

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

    inline fun <reified E: Reasoning> filterReasonings() = reasonings.filterIsInstance<E>()

    /***
     * returns: List of accepted Reasonings
     */
    fun addReasonings(reasonings: List<Reasoning>): List<Reasoning> {
        val toAccept = reasonings.filter { it !in _reasonings }
        _reasonings.addAll(toAccept)
        return toAccept
    }

    fun extractComplexFor(concept: ReasonConcept, name: String): ReasonComplex {
        val conceptReasonings = reasonings.filter { r ->
            r is EqualityReasoning && r.any { it == concept }
                    || r is ActionReasoning && (r.objectConcept == concept || r.actionConcept == concept)
                    || r is ExistenceInReasoning && (r.subjectConcept == concept || r.surroundingConcept == concept)
        }

        return ReasonComplex(name, conceptReasonings.toSet())
    }

    fun copy(name: String = this.name) = ReasonComplex(name, _reasonings.toSet())

    override fun toString() = """
        |$name
        |${reasonings.joinToString("\n")}
        """
}
