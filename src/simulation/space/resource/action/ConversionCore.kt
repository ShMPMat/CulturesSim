package simulation.space.resource.action

import simulation.SimulationException
import simulation.space.resource.Resource
import simulation.space.resource.instantiation.GenomeTemplate

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
                val resource = r?.copy(n)
                        ?: throw SimulationException("Empty conversion")
                return@map if (resource.core.genome is GenomeTemplate)
                    throw SimulationException("No GenomeTemplates allowed")
                else resource
            }

    fun copy() = ConversionCore(actionConversion)

    fun hasApplication(action: ResourceAction) = actionConversion.containsKey(action)
}
