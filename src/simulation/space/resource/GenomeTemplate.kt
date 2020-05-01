package simulation.space.resource

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
        genome.deathTime,
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

    fun getInstantiatedGenome(templateLegacy: ResourceCore): Genome {
        val genome = copy()
        genome.deathTime = templateLegacy.genome.deathTime
        genome.templateLegacy = templateLegacy
        genome.primaryMaterial = templateLegacy.genome.primaryMaterial
        genome.computeTagsFromMaterials()
        return genome
    }
}