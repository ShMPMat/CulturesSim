package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.space.resource.action.ResourceAction
import io.tashtabash.simulation.space.resource.specialActions


class ConversionParser(val actions: List<ResourceAction>, val dependencyParser: DependencyParser) {
    private val conversionPrefix = "~"
    private val dependencySeparator = '/'

    fun parse(conversionStr: String): Pair<ResourceAction, List<ResourceLink>> {
        val nameEndIndex = conversionStr.indexOf(dependencySeparator)
                .takeIf { it != -1 }
                ?: conversionStr.length
        val actionName = conversionStr.take(nameEndIndex)

        val action = specialActions[actionName]
                ?: parseProbabilityAction(actionName)
                ?: actions.firstOrNull { it.technicalName == actionName }
                ?: throw ParseException("Cannot parse action name '$actionName'")

        val resourceStrings = conversionStr.drop(nameEndIndex + 1)
                .split(dependencySeparator)
                .filter { it.isNotEmpty() }

        val resourceLinks = resourceStrings
                .filter { !it.startsWith(conversionPrefix) }
                .map { parseLink(it, this) }

        val dependencies = resourceStrings
                .filter { it.startsWith(conversionPrefix) }
                .map { s ->
                    val dependencyString = s.drop(conversionPrefix.length)

                    dependencyParser.parseUnsafe(dependencyString)
                }

        return action.copy(dependencies = dependencies) to resourceLinks
    }
}
