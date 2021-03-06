package shmp.simulation.space.resource.transformer

import shmp.simulation.space.resource.Resource


class PartsPipe(transformer: ResourceTransformer) : PipeTransformer(transformer) {
    override fun transform(resource: Resource): Resource {
        val newParts = resource.genome.parts.map { transformer.transform(it) }
        val genome = resource.genome.copy(parts = newParts)
        val core = resource.core.copy(genome = genome)
        return Resource(core)
    }
}
