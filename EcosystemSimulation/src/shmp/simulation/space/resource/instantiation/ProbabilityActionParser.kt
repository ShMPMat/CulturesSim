package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.action.ResourceProbabilityAction
import java.lang.Exception


fun parseProbabilityAction(string: String): ResourceProbabilityAction? {
    return try {
        val elements = string.split("_")

        if (elements.size != 5 || elements[0] != "" || elements[2] != "prob")
            return null

        elements[3].toDoubleOrNull()?.let { prob ->
            ResourceProbabilityAction(elements[1], prob, elements[4].toBoolean())
        }
    } catch (e: Exception) {
        null
    }
}
