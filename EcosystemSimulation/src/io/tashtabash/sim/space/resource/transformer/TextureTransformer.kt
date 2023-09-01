package io.tashtabash.sim.space.resource.transformer

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.ResourceTexture
import io.tashtabash.sim.space.resource.replaceRecursiveLinks


class TextureTransformer(private val texture: ResourceTexture) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(appearance = resource.genome.appearance.copy(texture = texture))
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
