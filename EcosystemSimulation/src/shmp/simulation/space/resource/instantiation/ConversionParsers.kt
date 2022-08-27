package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.specialActions


class ConversionParser(val actions: List<ResourceAction>, val dependencyParser: DependencyParser) {
    fun parse(conversionStr: String): Pair<ResourceAction, List<ResourceLink>> {
        val dependencyPrefix = "~"
        val nameEndIndex = conversionStr.indexOf('/')
                .takeIf { it != -1 }
                ?: throw ParseException("Cannot parse action '$conversionStr'")
        val actionName = conversionStr.take(nameEndIndex)

        val action = specialActions[actionName]
                ?: parseProbabilityAction(actionName)
                ?: actions.firstOrNull { it.name == actionName }
                ?: throw ParseException("Cannot parse action name '$actionName'")

        val resourceStrings = conversionStr.drop(nameEndIndex + 1)
                .split("/")

        val resourceLinks = resourceStrings
                .filter { !it.startsWith(dependencyPrefix) }
                .map { parseLink(it, this) }

        val dependencies = resourceStrings
                .filter { it.startsWith(dependencyPrefix) }
                .map { s ->
                    val dependencyString = s.drop(dependencyPrefix.length)

                    dependencyParser.parseUnsafe(dependencyString)
                }

        return action.copy(dependencies = dependencies) to resourceLinks
    }
}
