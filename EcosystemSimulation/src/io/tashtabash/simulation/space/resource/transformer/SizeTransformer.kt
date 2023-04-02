package io.tashtabash.simulation.space.resource.transformer

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.replaceRecursiveLinks


class SizeTransformer(val size: Double) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(sizeRange = size to size)
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
