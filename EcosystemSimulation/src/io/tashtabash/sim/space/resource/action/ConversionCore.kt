package io.tashtabash.sim.space.resource.action

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Resources


class ConversionCore(actionConversion: Map<ResourceAction, MutableList<Resource>>) {
    val actionConversions = mutableMapOf<ResourceAction, MutableList<Resource>>()
    internal val probabilityActions = mutableListOf<ResourceProbabilityAction>()
    internal val passiveActions = mutableListOf<ResourceProbabilityAction>()

    val allActionConversions: Map<ResourceAction, List<Resource>>
        get() = actionConversions + passiveActions.associateWith { listOf() }

    init {
        for ((a, rs) in actionConversion)
            addActionConversion(a, rs)
    }

    internal fun addActionConversion(action: ResourceAction, resources: Resources) {
        if (action is ResourceProbabilityAction) {
            if (resources.isEmpty() && action !in passiveActions)
                passiveActions.add(action)
            else if (action !in probabilityActions)
                probabilityActions.add(action)
        }

        actionConversions[action] = resources.toMutableList()
    }

    fun applyAction(action: ResourceAction): Resources? = actionConversions[action]
            ?.map { r ->
                r.copy(r.amount)
            }

    fun copy() = ConversionCore(actionConversions)

    fun hasApplication(action: ResourceAction) = actionConversions.containsKey(action)
}
