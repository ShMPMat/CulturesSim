package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.space.resource.*
import io.tashtabash.simulation.space.resource.action.ConversionCore
import io.tashtabash.simulation.space.resource.dependency.ResourceDependency
import io.tashtabash.simulation.space.resource.instantiation.tag.TagTemplate
import io.tashtabash.simulation.space.resource.material.Material
import io.tashtabash.simulation.space.resource.tag.ResourceTag


class GenomeTemplate(genome: Genome, val tagTemplates: List<TagTemplate>) : Genome(
        genome.name,
        genome.type,
        genome.sizeRange,
        genome.spreadProbability,
        genome.baseDesirability,
        genome.isMutable,
        genome.isMovable,
        genome.behaviour,
        genome.appearance,
        genome.isDesirable,
        genome.hasLegacy,
        genome.lifespan,
        genome.defaultAmount,
        genome.legacy,
        genome.dependencies,
        genome.tags,
        genome.primaryMaterial,
        genome.secondaryMaterials,
        genome.conversionCore
) {
    init {
        genome.parts.forEach { addPart(it) }
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
            isDesirable: Boolean,
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
    ): GenomeTemplate {
        val genome = super.copy(
                name,
                type,
                sizeRange,
                spreadProbability,
                baseDesirability,
                isMutable,
                isMovable,
                behaviour,
                appearance,
                isDesirable,
                hasLegacy,
                lifespan,
                defaultAmount,
                legacy,
                dependencies,
                tags,
                primaryMaterial,
                secondaryMaterials,
                conversionCore,
                parts
        )

        return GenomeTemplate(genome, tagTemplates)
    }

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
            isDesirable,
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
