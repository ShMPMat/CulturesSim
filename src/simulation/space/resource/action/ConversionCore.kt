package simulation.space.resource.action

import simulation.space.resource.Resource

class ConversionCore(actionConversion: Map<ResourceAction, MutableList<Pair<Resource?, Int>>>) {
    val actionConversion = mutableMapOf<ResourceAction, MutableList<Pair<Resource?, Int>>>()
    internal val probabilityActions = mutableListOf<ResourceProbabilityAction>()

    init {
        actionConversion.forEach { (a, rs) -> addActionConversion(a, rs) }
    }

    internal fun addActionConversion(action: ResourceAction, resourceList: List<Pair<Resource?, Int>>) {
        if (action is ResourceProbabilityAction)
            probabilityActions.add(action)

        actionConversion[action] = resourceList.toMutableList()
    }

    fun copy() = ConversionCore(actionConversion)

    fun hasApplication(action: ResourceAction) = actionConversion.containsKey(action)
}
