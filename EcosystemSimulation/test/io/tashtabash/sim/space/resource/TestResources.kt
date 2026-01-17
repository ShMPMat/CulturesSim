package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.tag.ResourceTag


fun createTestGenome(
    name: String = "Plant",
    legacy: String? = null,
    sizeRange: Pair<Double, Double> = 1.0 to 1.0,
    primaryMaterial: Material = Material("Fibre", 0.1, listOf()),
    tags: Set<ResourceTag> = emptySet(),
    actions: Map<ResourceAction, MutableList<Resource>> = mapOf()
): Genome {
    return Genome(
        name = name,
        type = ResourceType.Plant,
        sizeRange = sizeRange,
        spreadProbability = 0.5,
        baseDesirability = 10,
        isMutable = false,
        isMovable = false,
        behaviour = Behaviour(0.0, 0.0, 0.0, 0.0, OverflowType.Cut),
        appearance = Appearance(null, null, null),
        hasLegacy = legacy != null,
        lifespan = 100.0,
        defaultAmount = 10,
        legacy = legacy,
        dependencies = emptyList(),
        tags = tags,
        primaryMaterial = primaryMaterial,
        secondaryMaterials = emptyList(),
        conversionCore = ConversionCore(actions)
    )
}
