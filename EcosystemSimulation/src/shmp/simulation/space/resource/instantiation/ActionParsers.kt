package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.action.ResourceProbabilityAction
import java.lang.Exception


fun parseProbabilityAction(string: String): ResourceProbabilityAction? {
    return try {
        val tokens = string.split("_")

        if (tokens.size < 5 || tokens[0] != "" || tokens[2] != "prob")
            return null

        tokens[3].toDoubleOrNull()?.let { prob ->
            ResourceProbabilityAction(tokens[1], prob, tokens[4].parseBoolean(), tokens.getOrNull(5).parseBoolean())
        }
    } catch (e: Exception) {
        null
    }
}

private fun String?.parseBoolean() = this?.let {
    when (it) {
        "t", "true" -> true
        "f", "false" -> false
        else -> throw ParseException("Cannot read boolean value of '$it'")
    }
} ?: false
