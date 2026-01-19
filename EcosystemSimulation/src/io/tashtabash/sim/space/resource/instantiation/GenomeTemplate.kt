package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.sim.space.resource.*
import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.dependency.ResourceDependency
import io.tashtabash.sim.space.resource.instantiation.tag.TagTemplate
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.tag.ResourceTag


// A template which should be instantiated by a legacy Resource before it becomes a resource (like a house)
class GenomeTemplate(
    name: String,
    type: ResourceType,
    sizeRange: Pair<Double, Double>,
    spreadProbability: Double,
    baseDesirability: Int,
    isMutable: Boolean,
    isMovable: Boolean,
    behaviour: Behaviour,
    appearance: Appearance,
    hasLegacy: Boolean,
    lifespan: Double,
    defaultAmount: Int,
    legacy: BaseName?,
    dependencies: List<ResourceDependency>,
    tags: Set<ResourceTag>,
    primaryMaterial: Material?,
    secondaryMaterials: List<Material>,
    conversionCore: ConversionCore,
    val tagTemplates: List<TagTemplate>
) : Genome(
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
    lifespan,
    defaultAmount,
    legacy,
    dependencies,
    tags,
    primaryMaterial,
    secondaryMaterials,
    conversionCore
) {
    init {
        parts.forEach { addPart(it) }
    }

    override fun copy(
        name: String,
        type: ResourceType,
        sizeRange: Pair<Double, Double>,
        spreadProbability: Double,
        baseDesirability: Int,
        isMutable: Boolean,
        isMovable: Boolean,
        behaviour: Behaviour,
        appearance: Appearance,
        hasLegacy: Boolean,
        lifespan: Double,
        defaultAmount: Int,
        legacy: BaseName?,
        dependencies: List<ResourceDependency>,
        tags: Set<ResourceTag>,
        primaryMaterial: Material?,
        secondaryMaterials: List<Material>,
        conversionCore: ConversionCore,
        parts: List<Resource>
    ) = GenomeTemplate(
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
        lifespan,
        defaultAmount,
        legacy,
        dependencies,
        tags,
        primaryMaterial,
        secondaryMaterials,
        conversionCore,
        tagTemplates
    )

    private fun toGenome() = Genome(
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
        lifespan,
        defaultAmount,
        legacy,
        dependencies,
        tags,
        primaryMaterial,
        secondaryMaterials,
        conversionCore
    )


    fun getInstantiatedGenome(legacyGenome: Genome) = copy(
        lifespan = legacyGenome.lifespan,
        legacy = legacyGenome.baseName,
        primaryMaterial = legacyGenome.primaryMaterial,
        tags = legacyGenome.tags + this.tags.filter { it !in legacyGenome.tags }
    ).let {
        it.copy(tags = it.tags + tagTemplates.map { t -> t.initialize(it) }).toGenome()
    }
}
