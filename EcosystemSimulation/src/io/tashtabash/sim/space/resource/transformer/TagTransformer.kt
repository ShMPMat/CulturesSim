package io.tashtabash.sim.space.resource.transformer

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.replaceRecursiveLinks
import io.tashtabash.sim.space.resource.tag.ResourceTag


class TagTransformer(val tag: ResourceTag) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(tags = resource.genome.tags - tag + tag)
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
