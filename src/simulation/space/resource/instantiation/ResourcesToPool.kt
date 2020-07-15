package simulation.space.resource.instantiation

import simulation.SimulationException
import simulation.space.resource.ResourceIdeal
import simulation.space.resource.container.ResourcePool

fun finalizePool(startResources: List<ResourceIdeal>): ResourcePool {
    val finalizedResources = mutableListOf<ResourceIdeal>()
    val resourcesToAdd = mutableListOf<ResourceIdeal>()
    resourcesToAdd.addAll(startResources)

    while (resourcesToAdd.isNotEmpty()) {
        resourcesToAdd.firstOrNull { it.genome is GenomeTemplate }?.let { templateResource ->
            throw SimulationException("Template Resource in the final Resources - ${templateResource.baseName}")
        }

        finalizedResources.addAll(resourcesToAdd)
        val lastResources = resourcesToAdd.toList()
        resourcesToAdd.clear()

        resourcesToAdd.addAll(
                lastResources
                        .flatMap { it.genome.parts }
                        .map { ResourceIdeal(it.genome) }
        )
        resourcesToAdd.addAll(
                lastResources
                        .flatMap { it.genome.conversionCore.actionConversion.values }
                        .flatten()
                        .mapNotNull { (r) -> r?.genome } //TODO why are there nulls?
                        .map { ResourceIdeal(it) }
        )

        resourcesToAdd.removeIf { it in finalizedResources }
    }

    return ResourcePool(finalizedResources.sortedBy { it.baseName }.distinctBy { it.baseName })
}