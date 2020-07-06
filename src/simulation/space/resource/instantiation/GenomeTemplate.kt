package simulation.space.resource.instantiation

import simulation.space.resource.Genome
import simulation.space.resource.ResourceCore

class GenomeTemplate(genome: Genome) : Genome(
        genome.name,
        genome.type,
        genome.size,
        genome.spreadProbability,
        genome.temperatureMin,
        genome.temperatureMax,
        genome.baseDesirability,
        genome.canMove,
        genome.isMutable,
        genome.isMovable,
        genome.isResisting,
        genome.isDesirable,
        genome.hasLegacy,
        genome.lifespan,
        genome.defaultAmount,
        genome.legacy,
        genome.templateLegacy,
        genome.dependencies,
        genome.tags,
        genome.primaryMaterial,
        genome.secondaryMaterials
) {
    init {
        genome.parts.forEach { addPart(it) }
    }

    fun getInstantiatedGenome(templateLegacy: ResourceCore) = copy(
            deathTime = templateLegacy.genome.lifespan,
            templateLegacy = templateLegacy,
            primaryMaterial = templateLegacy.genome.primaryMaterial
    )
}