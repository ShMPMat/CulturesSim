package simulation.space.resource.labeler

import simulation.space.resource.material.Material
import simulation.space.resource.ResourceIdeal

class MaterialLabeler(private val material: Material) : ResourceTagLabeler {
    override fun isSuitable(resource: ResourceIdeal) = resource.genome.primaryMaterial == material
}