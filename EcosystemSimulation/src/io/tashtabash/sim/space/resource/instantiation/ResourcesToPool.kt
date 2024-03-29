package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.space.resource.ResourceIdeal
import io.tashtabash.sim.space.resource.container.ResourcePool


fun finalizePool(startResources: List<ResourceIdeal>): ResourcePool {
    val finalizedResources = mutableListOf<ResourceIdeal>()
    val resourcesToAdd = mutableListOf<ResourceIdeal>()
    resourcesToAdd.addAll(startResources)

    while (resourcesToAdd.isNotEmpty()) {
        resourcesToAdd.firstOrNull { it.genome is GenomeTemplate }?.let { templateResource ->
            throw DataInitializationError("Template Resource in the final Resources - ${templateResource.baseName}")
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
                        .flatMap { it.genome.conversionCore.actionConversions.values }
                        .flatten()
                        .map { r -> r.genome }
                        .map { ResourceIdeal(it) }
        )

        resourcesToAdd.removeIf { it in finalizedResources }
    }

    return ResourcePool(finalizedResources.sortedBy { it.baseName }.distinctBy { it.baseName }.map { it.core })
}
