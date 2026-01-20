package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.space.resource.ResourceIdeal
import io.tashtabash.sim.space.resource.container.ResourcePool


fun listAllResources(startResources: List<ResourceIdeal>): List<ResourceIdeal> {
    val finalizedResources = mutableListOf<ResourceIdeal>()
    val resourcesToAdd = mutableListOf<ResourceIdeal>()
    resourcesToAdd += startResources

    while (resourcesToAdd.isNotEmpty()) {
        resourcesToAdd.firstOrNull { it.genome is GenomeTemplate }?.let { templateResource ->
            throw DataInitializationError("Template Resource in the final Resources - ${templateResource.baseName}")
        }

        finalizedResources += resourcesToAdd
        val lastResources = resourcesToAdd.toList()
        resourcesToAdd.clear()

        resourcesToAdd += lastResources
            .flatMap { it.genome.parts }
            .map { ResourceIdeal(it.genome) }
        resourcesToAdd += lastResources
            .flatMap { it.genome.conversionCore.actionConversions.values }
            .flatten()
            .map { r -> r.genome }
            .map { ResourceIdeal(it) }

        resourcesToAdd.removeIf { it in finalizedResources }
    }

    return finalizedResources.distinctBy { it.baseName }
}

fun finalizePool(finalizedResources: List<ResourceIdeal>) = ResourcePool(
    finalizedResources.sortedBy { it.baseName }.map { it.core }
)
