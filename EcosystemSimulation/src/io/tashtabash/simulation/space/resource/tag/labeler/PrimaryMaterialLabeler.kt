package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.material.Material


data class PrimaryMaterialLabeler(private val material: Material) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.primaryMaterial == material

    override fun toString() = "The primary material is ${material.name}"
}
