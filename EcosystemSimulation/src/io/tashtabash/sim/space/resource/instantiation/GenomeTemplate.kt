package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.sim.space.resource.*
import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.dependency.ResourceDependency
import io.tashtabash.sim.space.resource.instantiation.tag.TagTemplate
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.tag.ResourceTag


// A template which should be instantiated by a legacy Resource before it becomes a resource (like a house)
class GenomeTemplate(
    val name: String,
    val type: ResourceType,
    val sizeRange: Pair<Double, Double>,
    val spreadProbability: Double,
    val baseDesirability: Int,
    val isMutable: Boolean,
    val isMovable: Boolean,
    val behaviour: Behaviour,
    val appearance: Appearance,
    val hasLegacy: Boolean,
    val lifespan: Double,
    val defaultAmount: Int,
    val legacy: BaseName?,
    val dependencies: List<ResourceDependency>,
    val tags: Set<ResourceTag>,
    val primaryMaterial: Material?,
    val secondaryMaterials: List<Material>,
    val conversionCore: ConversionCore,
    val tagTemplates: List<TagTemplate>,
    val parts: MutableList<Resource> = mutableListOf()
) {
    fun getInstantiatedGenome(legacyGenome: Genome) = Genome(
        name,
        type,
        sizeRange,
        spreadProbability,
        baseDesirability,
        isMutable,
        isMovable,
        behaviour,
        appearance,
        hasLegacy,
        legacyGenome.lifespan,
        defaultAmount,
        legacyGenome.baseName,
        dependencies,
        legacyGenome.tags + tags.filter { it !in legacyGenome.tags },
        legacyGenome.primaryMaterial,
        secondaryMaterials,
        conversionCore
    ).let {
        it.copy(tags = it.tags + tagTemplates.map { t -> t.initialize(it) })
    }
}
