package shmp.simulation.space.resource.transformer

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.flingConversionLinks

class SizeTransformer(val size: Double) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(size = size)
        val core = resource.core.copy(genome = genome)
        return Resource(core).flingConversionLinks(resource)
    }
}
