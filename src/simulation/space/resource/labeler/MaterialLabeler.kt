package simulation.space.resource.labeler

import simulation.space.resource.material.Material
import simulation.space.resource.Resource

class MaterialLabeler(private val material: Material) : ResourceTagLabeler {
    override fun isSuitable(resource: Resource) = resource.genome.primaryMaterial == material
}