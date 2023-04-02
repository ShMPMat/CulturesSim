package io.tashtabash.simulation.space.resource.action

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.Resources


class ConversionCore(actionConversion: Map<ResourceAction, MutableList<Resource>>) {
    val actionConversion = mutableMapOf<ResourceAction, MutableList<Resource>>()
    internal val probabilityActions = mutableListOf<ResourceProbabilityAction>()

    init {
        actionConversion.forEach { (a, rs) -> addActionConversion(a, rs) }
    }

    internal fun addActionConversion(action: ResourceAction, resources: Resources) {
        if (action is ResourceProbabilityAction)
            if (action !in probabilityActions)
                probabilityActions.add(action)

        actionConversion[action] = resources.toMutableList()
    }

    //TODO get rid of Templates
    fun applyAction(action: ResourceAction): Resources? = actionConversion[action]
            ?.map { r ->
                /*val resource = */r.copy(r.amount)

//                if (resource.genome is GenomeTemplate)
//                    throw SimulationError("No GenomeTemplates allowed")
//                else resource
            }

    fun copy() = ConversionCore(actionConversion)

    fun hasApplication(action: ResourceAction) = actionConversion.containsKey(action)
}
