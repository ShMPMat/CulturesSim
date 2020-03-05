package simulation.space.resource

class GenomeTemplate(genome: Genome) : Genome(genome) {
    fun getInstantiatedGenome(templateLegacy: ResourceCore): Genome {
        val genome = Genome(this)
        genome.deathTime = templateLegacy.deathTime
        genome.templateLegacy = templateLegacy
        primaryMaterial = templateLegacy.genome.primaryMaterial
        return genome
    }
}