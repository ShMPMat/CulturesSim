package io.tashtabash.sim.space.resource.transformer

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.replaceRecursiveLinks


class SizeTransformer(val size: Double) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(sizeRange = size to size)
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
