package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.Genome


class GenomeTemplate(genome: Genome) : Genome(
        genome.name,
        genome.type,
        genome.size,
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

    fun getInstantiatedGenome(legacyGenome: Genome) = copy(
            lifespan = legacyGenome.lifespan,
            legacy = legacyGenome.baseName,
            primaryMaterial = legacyGenome.primaryMaterial
    )
}
