package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome
import simulation.space.resource.material.Material

data class PrimaryMaterialLabeler(private val material: Material) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.primaryMaterial == material
}