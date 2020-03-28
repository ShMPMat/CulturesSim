package simulation.space.resource

class GenomeTemplate(genome: Genome) : Genome(genome) {
    fun getInstantiatedGenome(templateLegacy: ResourceCore): Genome {
        val genome = Genome(this)
        genome.deathTime = templateLegacy.genome.deathTime
        genome.templateLegacy = templateLegacy
        primaryMaterial = templateLegacy.genome.primaryMaterial
        genome.computeTagsFromMaterials()
        return genome
    }
}