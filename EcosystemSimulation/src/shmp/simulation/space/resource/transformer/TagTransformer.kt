package shmp.simulation.space.resource.transformer

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.flingConversionLinks
import shmp.simulation.space.resource.tag.ResourceTag


class TagTransformer(val tag: ResourceTag) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(tags = resource.genome.tags + listOf(tag))
        val core = resource.core.copy(genome = genome)
        return Resource(core).flingConversionLinks(resource)
    }
}
