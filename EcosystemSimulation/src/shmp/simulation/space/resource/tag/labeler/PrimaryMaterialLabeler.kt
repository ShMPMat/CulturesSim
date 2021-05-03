package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.material.Material


data class PrimaryMaterialLabeler(private val material: Material) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.primaryMaterial == material

    override fun toString() = "The primary material is ${material.name}"
}
