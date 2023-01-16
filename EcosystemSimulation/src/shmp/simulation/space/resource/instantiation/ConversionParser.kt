package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.specialActions


class ConversionParser(val actions: List<ResourceAction>, val dependencyParser: DependencyParser) {
    private val conversionPrefix = "~"
    private val dependencySeparator = '/'

    fun parse(conversionStr: String): Pair<ResourceAction, List<ResourceLink>> {
        val nameEndIndex = conversionStr.indexOf(dependencySeparator)
                .takeIf { it != -1 }
                ?: throw ParseException("Cannot parse action '$conversionStr'")
        val actionName = conversionStr.take(nameEndIndex)

        val action = specialActions[actionName]
                ?: parseProbabilityAction(actionName)
                ?: actions.firstOrNull { it.name == actionName }
                ?: throw ParseException("Cannot parse action name '$actionName'")

        val resourceStrings = conversionStr.drop(nameEndIndex + 1)
                .split(dependencySeparator)

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
