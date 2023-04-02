package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.DataInitializationError
import io.tashtabash.simulation.space.resource.ResourceIdeal
import io.tashtabash.simulation.space.resource.container.ResourcePool


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
                        .flatMap { it.genome.conversionCore.actionConversion.values }
                        .flatten()
                        .map { r -> r.genome }
                        .map { ResourceIdeal(it) }
        )

        resourcesToAdd.removeIf { it in finalizedResources }
    }

    return ResourcePool(finalizedResources.sortedBy { it.baseName }.distinctBy { it.baseName }.map { it.core })
}
