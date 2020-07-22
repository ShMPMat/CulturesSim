package simulation.space.resource.instantiation

import simulation.space.resource.action.ResourceAction
import simulation.space.resource.specialActions


fun parseConversion(conversionStr: String, actions: List<ResourceAction>) : Pair<ResourceAction, List<ResourceLink>> {
    val actionName = conversionStr.substring(0, conversionStr.indexOf(':'))

    val action = specialActions[actionName]
            ?: parseProbabilityAction(actionName)
            ?: actions.first { it.name == actionName }

    val resourceLinks = conversionStr.substring(conversionStr.indexOf(':') + 1)
            .split(",")
            .map { parseLink(it, actions) }

    return action to resourceLinks
}
