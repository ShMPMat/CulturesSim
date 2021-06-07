package shmp.simulation.space.resource.action

import shmp.simulation.space.resource.Resource


class ConversionCore(actionConversion: Map<ResourceAction, MutableList<Resource>>) {
    val actionConversion = mutableMapOf<ResourceAction, MutableList<Resource>>()
    internal val probabilityActions = mutableListOf<ResourceProbabilityAction>()

    init {
        actionConversion.forEach { (a, rs) -> addActionConversion(a, rs) }
    }

    internal fun addActionConversion(action: ResourceAction, resourceList: List<Resource>) {
        if (action is ResourceProbabilityAction)
            probabilityActions.add(action)

        actionConversion[action] = resourceList.toMutableList()
    }

    //TODO get rid of Templates
    fun applyAction(action: ResourceAction): List<Resource>? = actionConversion[action]
            ?.map { r ->
                /*val resource = */r.copy(r.amount)

//                if (resource.genome is GenomeTemplate)
//                    throw SimulationError("No GenomeTemplates allowed")
//                else resource
            }

    fun copy() = ConversionCore(actionConversion)

    fun hasApplication(action: ResourceAction) = actionConversion.containsKey(action)
}
