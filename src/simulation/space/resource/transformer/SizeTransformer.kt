package simulation.space.resource.transformer

import simulation.space.resource.Resource
import simulation.space.resource.flingConversionLinks

class SizeTransformer(val size: Double) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(size = size)
        val core = resource.core.copyCore(genome = genome)
        return Resource(core).flingConversionLinks(resource)
    }
}
