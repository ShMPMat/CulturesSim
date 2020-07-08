package simulation.space.resource

class ConversionCore(actionConversion: Map<ResourceAction, MutableList<Pair<Resource?, Int>>>) {
    val actionConversion = actionConversion.toMutableMap()

    internal fun addActionConversion(action: ResourceAction, resourceList: List<Pair<Resource?, Int>>) {
        actionConversion[action] = resourceList.toMutableList()
    }

    fun copy() = ConversionCore(actionConversion)

    fun hasApplication(action: ResourceAction) = actionConversion.containsKey(action)
}
