package io.tashtabash.sim.space.resource.transformer

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler


class LabelerPipe(transformer: ResourceTransformer, val labeler: ResourceLabeler) : PipeTransformer(transformer) {
    override fun transform(resource: Resource): Resource {
        val newResource =
                if (labeler.isSuitable(resource.genome))
                    transformer.transform(resource)
                else
                    resource.fullCopy()
        val newParts = newResource.genome.parts.map { transform(it) }
        val genome = newResource.genome.copy(parts = newParts)
        val core = newResource.core.copy(genome = genome)
        return Resource(core, resource.amount)
    }
}
