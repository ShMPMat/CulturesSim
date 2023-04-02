package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.DataInitializationError
import io.tashtabash.simulation.space.resource.dependency.AvoidDependency
import io.tashtabash.simulation.space.resource.dependency.ConsumeDependency
import io.tashtabash.simulation.space.resource.dependency.NeedDependency
import io.tashtabash.simulation.space.resource.dependency.ResourceDependency
import io.tashtabash.simulation.space.resource.tag.labeler.QuantifiedResourceLabeler
import io.tashtabash.simulation.space.resource.tag.labeler.makeResourceLabeler


interface DependencyParser {
    // Returns null when matching dependency type not found
    fun parse(tag: String): ResourceDependency?

    fun parseUnsafe(tag: String) = parse(tag)
            ?: throw DataInitializationError("Unknown dependency with tags: $tag")
}

open class DefaultDependencyParser : DependencyParser {
    override fun parse(tag: String): ResourceDependency? {
        val elements = tag.split(";")
                .toTypedArray()

        return when (elements[4]) {
            "CONSUME" -> ConsumeDependency(
                    parseDeprivationCoefficient(elements[2]),
                    parseIsNecessary(elements[3]),
                    QuantifiedResourceLabeler(makeResourceLabeler(elements[0]), elements[1].toDouble()),
                    elements.getOrNull(5)?.toInt() ?: 1
            )
            "AVOID" -> AvoidDependency(
                    parseDeprivationCoefficient(elements[2]),
                    parseIsNecessary(elements[3]),
                    QuantifiedResourceLabeler(makeResourceLabeler(elements[0]), elements[1].toDouble())
            )
            "EXIST" -> NeedDependency(
                    parseDeprivationCoefficient(elements[2]),
                    parseIsNecessary(elements[3]),
                    QuantifiedResourceLabeler(makeResourceLabeler(elements[0]), elements[1].toDouble())
            )
            else -> null
        }
    }

    private fun parseDeprivationCoefficient(str: String): Double {
        val numericValue = str.toDoubleOrNull()
                ?: throw ParseException("Cannot parse dependency deprivation coefficient of value '$str'")

        if (numericValue <= 1)
            throw ParseException("Dependency deprivation coefficient must be larger than 1")

        return numericValue
    }


    private fun parseIsNecessary(str: String): Boolean = when (str) {
        "1" -> true
        "0" -> false
        else -> throw ParseException("Dependency necessity must be either 0 or 1")
    }
}
