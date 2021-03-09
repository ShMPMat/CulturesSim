package shmp.simulation.space.resource.action

import shmp.simulation.SimulationError
import shmp.simulation.space.resource.Resource


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

    //TODO get rid of Templates
    fun applyAction(action: ResourceAction): List<Resource>? = actionConversion[action]
            ?.map { (r, n) ->
                /*val resource = */r?.copy(n)
                        ?: throw SimulationError("Empty conversion")

//                if (resource.genome is GenomeTemplate)
//                    throw SimulationError("No GenomeTemplates allowed")
//                else resource
            }

    fun copy() = ConversionCore(actionConversion)

    fun hasApplication(action: ResourceAction) = actionConversion.containsKey(action)
}
