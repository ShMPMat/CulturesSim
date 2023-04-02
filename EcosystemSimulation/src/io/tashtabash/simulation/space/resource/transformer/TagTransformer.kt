package io.tashtabash.simulation.space.resource.transformer

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.replaceRecursiveLinks
import io.tashtabash.simulation.space.resource.tag.ResourceTag


class TagTransformer(val tag: ResourceTag) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(tags = resource.genome.tags + listOf(tag))
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
