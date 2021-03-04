package shmp.simulation.culture.group.cultureaspect.reasoning

import shmp.utils.SoftValue


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
        val toAccept = reasonings.filter { it !in _reasonings }
        _reasonings.addAll(toAccept)
        return toAccept
    }

    override fun toString() = """
        |$name
        |${reasonings.joinToString("\n")}
        """
}
