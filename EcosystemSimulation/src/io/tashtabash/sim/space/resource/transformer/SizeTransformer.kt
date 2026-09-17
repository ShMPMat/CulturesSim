package io.tashtabash.sim.space.resource.transformer

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.replaceRecursiveLinks


class SizeTransformer(val size: Double) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val oldSize = resource.genome.size
        val scale = size / oldSize.max
        val genome = resource.genome.copy(sizeRange = oldSize * scale to oldSize * scale)
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
