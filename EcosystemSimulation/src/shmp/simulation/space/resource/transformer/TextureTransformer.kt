package shmp.simulation.space.resource.transformer

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceTexture
import shmp.simulation.space.resource.replaceRecursiveLinks


class TextureTransformer(private val texture: ResourceTexture) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(appearance = resource.genome.appearance.copy(texture = texture))
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
