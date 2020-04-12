package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

data class SmallerDensityLabeler(private val density: Double): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.primaryMaterial.density <= density
}