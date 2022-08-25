package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.specialActions


class ConversionParser(val actions: List<ResourceAction>, val dependencyParser: DependencyParser) {
    fun parse(conversionStr: String): Pair<ResourceAction, List<ResourceLink>> {
        val dependencyPrefix = "~"
        val actionName = conversionStr.substring(0, conversionStr.indexOf(':'))

        val action = specialActions[actionName]
                ?: parseProbabilityAction(actionName)
                ?: actions.first { it.name == actionName }

        val resourceStrings = conversionStr.drop(conversionStr.indexOf(':') + 1)
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
