package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.material.Material


data class PrimaryMaterialLabeler(private val material: Material) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.primaryMaterial == material

    override fun toString() = "The primary material is ${material.name}"
}
