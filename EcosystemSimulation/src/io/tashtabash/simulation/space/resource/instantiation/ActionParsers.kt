package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.space.resource.action.ResourceProbabilityAction


fun parseProbabilityAction(string: String): ResourceProbabilityAction? {
    return try {
        val tokens = string.split("_")

        if (tokens.size < 5 || tokens[0] != "" || tokens[2] != "prob")
            return null

        val prob = tokens[3].toDoubleOrNull()
                ?: return null
        if (prob !in 0.0..1.0)
            throw ParseException("Probability action must have probability in 0..1")

        val isWasting = tokens[4].parseBoolean()
        val canChooseTile = tokens.getOrNull(5).parseBoolean()

        ResourceProbabilityAction(tokens[1], prob, isWasting, canChooseTile, listOf())
    } catch (e: ParseException) {
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
